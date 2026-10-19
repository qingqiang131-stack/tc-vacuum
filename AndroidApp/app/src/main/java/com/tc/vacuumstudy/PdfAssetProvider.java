package com.tc.vacuumstudy;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;
import java.io.IOException;

public class PdfAssetProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "application/pdf";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String name = documentName(uri);
        String[] columns = projection == null
                ? new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}
                : projection;
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        long size = -1;
        try (AssetFileDescriptor file = getContext().getAssets().openFd(name)) {
            size = file.getLength();
        } catch (IOException ignored) {
            // The viewer can still use the descriptor even if size metadata is unavailable.
        }
        Object[] row = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(columns[i])) row[i] = name;
            else if (OpenableColumns.SIZE.equals(columns[i])) row[i] = size;
        }
        cursor.addRow(row);
        return cursor;
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Read-only document");
        try {
            return getContext().getAssets().openFd(documentName(uri));
        } catch (IOException error) {
            throw new FileNotFoundException(error.getMessage());
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        try (AssetFileDescriptor descriptor = openAssetFile(uri, mode)) {
            return ParcelFileDescriptor.dup(descriptor.getFileDescriptor());
        } catch (IOException error) {
            throw new FileNotFoundException(error.getMessage());
        }
    }

    private String documentName(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null) throw new IllegalArgumentException("Missing document name");
        return name;
    }

    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { throw new UnsupportedOperationException(); }
}
