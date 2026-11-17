package com.github.salilvnair.excelprocessor.v2.processor.helper.style;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Workbook-scoped cache of {@link CellStyle}s and {@link Font}s.
 * <p>
 * POI workbooks have a hard limit on the number of cell styles (64,000 for XLSX, 4,000 for XLS)
 * and every {@code createCellStyle()}/{@code createFont()} call adds a permanent entry. Creating
 * styles per cell therefore fails on large sheets. All library code creates styles through this
 * registry, which returns one shared style per distinct {@link CellStyleSpec}.
 * <p>
 * Shared styles must be treated as <b>immutable</b>: never call setters on a style obtained from a
 * cell written by the library. Use {@link #resolve} or {@link #intern} to obtain a variant instead.
 * <p>
 * A registry lives as long as one {@code ExcelSheetContext}. It keeps per-workbook state keyed by
 * workbook identity and is never shared globally, so it does not outlive the workbooks it serves.
 * It is not thread-safe, matching POI workbooks themselves.
 *
 * @author Salil V Nair
 */
public final class CellStyleRegistry {
    /** fraction of the format's style limit at which a diagnostic exception is raised */
    private static final double STYLE_LIMIT_SAFETY_FACTOR = 0.98;
    private static final int XLS_MAX_CELL_STYLES = 4000;

    private final Map<Workbook, PerWorkbook> workbooks = new IdentityHashMap<>();

    private static final class PerWorkbook {
        final Map<CellStyleSpec, CellStyle> styles = new HashMap<>();
        final Map<FontSpec, Font> fonts = new HashMap<>();
        final Map<StyleSignature, CellStyle> interned = new HashMap<>();
        final Map<CellStyle, CellStyle> copiedStyles = new IdentityHashMap<>();
        /** (style source object identity, base style index) -> resolved style; skips spec building per cell */
        final Map<Object, Map<Integer, CellStyle>> memo = new IdentityHashMap<>();
        CellStyle taskScratch;
        int stylesCreated;
        int fontsCreated;
    }

    private PerWorkbook state(Workbook workbook) {
        return workbooks.computeIfAbsent(workbook, w -> new PerWorkbook());
    }

    /** Returns the shared style for {@code spec}, creating it on first use. */
    public CellStyle resolve(Workbook workbook, CellStyleSpec spec) {
        PerWorkbook state = state(workbook);
        CellStyle style = state.styles.get(spec);
        if (style == null) {
            style = newStyle(workbook, state, spec);
            spec.applyTo(style, workbook, this);
            state.interned.putIfAbsent(StyleSignature.of(style), style);
            state.styles.put(spec, style);
        }
        return style;
    }

    /**
     * Fast lookup of a style previously resolved for an immutable style source (for example a
     * cached annotation info) on top of {@code baseStyleIndex}; {@code null} when not resolved yet.
     */
    public CellStyle memo(Workbook workbook, Object source, int baseStyleIndex) {
        Map<Integer, CellStyle> byBase = state(workbook).memo.get(source);
        return byBase == null ? null : byBase.get(baseStyleIndex);
    }

    public void putMemo(Workbook workbook, Object source, int baseStyleIndex, CellStyle style) {
        state(workbook).memo.computeIfAbsent(source, s -> new HashMap<>()).put(baseStyleIndex, style);
    }

    /** Returns the shared font for {@code spec}, creating it on first use. */
    public Font font(Workbook workbook, FontSpec spec) {
        PerWorkbook state = state(workbook);
        Font font = state.fonts.get(spec);
        if (font == null) {
            font = workbook.createFont();
            spec.applyTo(font);
            state.fonts.put(spec, font);
            state.fontsCreated++;
        }
        return font;
    }

    /**
     * Returns a shared style equal in every visual property to {@code candidate}. If no such style
     * is known yet, a new style is created as a copy of {@code candidate}, so {@code candidate}
     * itself may keep being reused as a scratch style.
     */
    public CellStyle intern(Workbook workbook, CellStyle candidate) {
        PerWorkbook state = state(workbook);
        StyleSignature signature = StyleSignature.of(candidate);
        CellStyle style = state.interned.get(signature);
        if (style == null) {
            style = newStyle(workbook, state, "interned style");
            style.cloneStyleFrom(candidate);
            state.interned.put(signature, style);
        }
        return style;
    }

    /**
     * Copies a style that may belong to another workbook. Every distinct source style is copied at
     * most once per destination workbook.
     */
    public CellStyle copyOf(Workbook destination, CellStyle source) {
        PerWorkbook state = state(destination);
        CellStyle style = state.copiedStyles.get(source);
        if (style == null) {
            style = newStyle(destination, state, "copy of style " + source.getIndex());
            style.cloneStyleFrom(source);
            state.copiedStyles.put(source, style);
        }
        return style;
    }

    /**
     * A per-workbook scratch style handed to user style tasks, so that a task can mutate "the
     * cell's style" without corrupting a shared style. Pass it to {@link #intern} afterwards.
     */
    public CellStyle taskScratch(Workbook workbook) {
        PerWorkbook state = state(workbook);
        if (state.taskScratch == null) {
            state.taskScratch = newStyle(workbook, state, "task scratch style");
        }
        return state.taskScratch;
    }

    /** Number of cell styles this registry has created in {@code workbook}. */
    public int stylesCreated(Workbook workbook) {
        PerWorkbook state = workbooks.get(workbook);
        return state == null ? 0 : state.stylesCreated;
    }

    /** Number of fonts this registry has created in {@code workbook}. */
    public int fontsCreated(Workbook workbook) {
        PerWorkbook state = workbooks.get(workbook);
        return state == null ? 0 : state.fontsCreated;
    }

    private CellStyle newStyle(Workbook workbook, PerWorkbook state, Object description) {
        int limit = workbook instanceof HSSFWorkbook ? XLS_MAX_CELL_STYLES : SpreadsheetVersion.EXCEL2007.getMaxCellStyles();
        int current = workbook.getNumCellStyles();
        if (current >= (int) (limit * STYLE_LIMIT_SAFETY_FACTOR)) {
            throw new IllegalStateException("Workbook cell style limit almost reached (" + current + " of " + limit
                    + " styles, " + state.stylesCreated + " created by excelprocessor) while creating " + description
                    + ". Too many distinct styles: check custom style tasks that call createCellStyle() per cell.");
        }
        state.stylesCreated++;
        return workbook.createCellStyle();
    }
}
