package android.print;

import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import java.io.File;

public class PdfPrintHelper {
    public interface Callback {
        void onSuccess(File file);
        void onError(String error);
    }

    public static void print(final PrintDocumentAdapter adapter, final PrintAttributes attributes, final File outputFile, final Callback callback) {
        adapter.onLayout(null, attributes, new CancellationSignal(), new PrintDocumentAdapter.LayoutResultCallback() {
            @Override
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                try {
                    final ParcelFileDescriptor pfd = ParcelFileDescriptor.open(
                        outputFile,
                        ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE
                    );
                    adapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, pfd, new CancellationSignal(), new PrintDocumentAdapter.WriteResultCallback() {
                        @Override
                        public void onWriteFinished(PageRange[] pages) {
                            super.onWriteFinished(pages);
                            try {
                                pfd.close();
                            } catch (Exception ignored) {}
                            callback.onSuccess(outputFile);
                        }

                        @Override
                        public void onWriteFailed(CharSequence error) {
                            super.onWriteFailed(error);
                            try {
                                pfd.close();
                            } catch (Exception ignored) {}
                            callback.onError(error != null ? error.toString() : "فشل كتابة ملف PDF");
                        }

                        @Override
                        public void onWriteCancelled() {
                            super.onWriteCancelled();
                            try {
                                pfd.close();
                            } catch (Exception ignored) {}
                            callback.onError("تم إلغاء كتابة ملف PDF");
                        }
                    });
                } catch (Exception e) {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "خطأ أثناء إنشاء ملف PDF");
                }
            }

            @Override
            public void onLayoutFailed(CharSequence error) {
                super.onLayoutFailed(error);
                callback.onError(error != null ? error.toString() : "فشل تخطيط صفحة PDF");
            }

            @Override
            public void onLayoutCancelled() {
                super.onLayoutCancelled();
                callback.onError("تم إلغاء تخطيط PDF");
            }
        }, null);
    }
}
