package com.github.salilvnair.excelprocessor.v2.processor.helper;

import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetReaderException;
import com.github.salilvnair.excelprocessor.v2.exception.ExcelSheetWriterException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Password-protected workbooks. XLSX files are read and written with ECMA-376 agile encryption
 * (what Excel uses for "Encrypt with Password"); XLS files with the BIFF8 RC4 scheme.
 */
public final class WorkbookCrypto {
    private WorkbookCrypto() {}

    /** True when the file is an OLE2 container holding an encrypted OOXML package. */
    public static boolean isEncrypted(File file) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            if (FileMagic.valueOf(in) != FileMagic.OLE2) {
                return false;
            }
        }
        catch (IOException e) {
            return false;
        }
        try (POIFSFileSystem fs = new POIFSFileSystem(file, true)) {
            return fs.getRoot().hasEntry(Decryptor.DEFAULT_POIFS_ENTRY);
        }
        catch (IOException e) {
            return false;
        }
    }

    /**
     * Decrypts an encrypted XLSX into a temporary file and returns its path. The caller deletes it.
     * Decrypting to disk keeps memory flat for the streaming reader.
     */
    public static Path decryptToTempFile(File file, String password) {
        try (POIFSFileSystem fs = new POIFSFileSystem(file, true)) {
            return decryptToTempFile(fs, password);
        }
        catch (IOException e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    public static Path decryptToTempFile(InputStream inputStream, String password) {
        try (POIFSFileSystem fs = new POIFSFileSystem(inputStream)) {
            return decryptToTempFile(fs, password);
        }
        catch (IOException e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    private static Path decryptToTempFile(POIFSFileSystem fs, String password) throws IOException {
        Decryptor decryptor;
        try {
            decryptor = Decryptor.getInstance(new EncryptionInfo(fs));
            if (!decryptor.verifyPassword(password == null ? Decryptor.DEFAULT_PASSWORD : password)) {
                throw new ExcelSheetReaderException("The password does not open this workbook");
            }
        }
        catch (java.security.GeneralSecurityException e) {
            throw new ExcelSheetReaderException(e);
        }
        Path temp = Files.createTempFile("excelprocessor-decrypted", ".xlsx");
        try (InputStream data = decryptor.getDataStream(fs)) {
            Files.copy(data, temp, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException | java.security.GeneralSecurityException | RuntimeException e) {
            Files.deleteIfExists(temp);
            if (e instanceof ExcelSheetReaderException) {
                throw (ExcelSheetReaderException) e;
            }
            throw new ExcelSheetReaderException(e);
        }
        return temp;
    }

    /**
     * Opens a workbook (XLS or XLSX), decrypting it with {@code password} when it is encrypted, and
     * closes the stream.
     */
    public static Workbook open(InputStream inputStream, String password) {
        try (InputStream in = inputStream) {
            return WorkbookFactory.create(in, password);
        }
        catch (org.apache.poi.EncryptedDocumentException e) {
            throw new ExcelSheetReaderException("The password does not open this workbook");
        }
        catch (IOException e) {
            throw new ExcelSheetReaderException(e);
        }
    }

    /**
     * Writes {@code workbook} to {@code out}, encrypted with {@code password} when it is not null.
     * The encrypted package is assembled in memory before it is written.
     */
    public static void write(Workbook workbook, OutputStream out, String password) throws IOException {
        if (password == null) {
            workbook.write(out);
            return;
        }
        if (workbook instanceof HSSFWorkbook) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
            try {
                workbook.write(out);
            }
            finally {
                Biff8EncryptionKey.setCurrentUserPassword(null);
            }
            return;
        }
        try (POIFSFileSystem fs = new POIFSFileSystem()) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(password);
            try (OutputStream encrypted = encryptor.getDataStream(fs)) {
                workbook.write(encrypted);
            }
            fs.writeFilesystem(out);
        }
        catch (java.security.GeneralSecurityException e) {
            throw new ExcelSheetWriterException(e);
        }
    }
}
