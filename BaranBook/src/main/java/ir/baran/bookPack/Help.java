package ir.baran.bookPack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * مدیریت نمایش دیالوگ راهنما برای صفحه بازی.
 */
public final class Help {

    private Help() {
    }

    public static void showGameHelpDialog(Context context, String titleText, String hintText) {
        View content = LayoutInflater.from(context).inflate(ir.baran.baranBook.R.layout.dialog_game_help, null, false);
        TextView body = content.findViewById(ir.baran.baranBook.R.id.helpBody);
        String fullMessage = titleText + "\n\n" + hintText;
        body.setText(fullMessage);

        new MaterialAlertDialogBuilder(context)
                .setView(content)
                .setPositiveButton(ir.baran.baranBook.R.string.game_help_dialog_close, null)
                .show();
    }
}
