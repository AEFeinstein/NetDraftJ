package com.gelakinetic.NetDraftJ.Client;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

class TextInputFilter extends DocumentFilter {
    public enum inputType {
        USERNAME, IP_ADDRESS
    }

    private inputType mInputType;

    public TextInputFilter(inputType type) {
        mInputType = type;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (test(sb.toString())) {
            super.insertString(fb, offset, string, attr);
        }
        else {
            // warn the user and don't allow the insert
        }
    }

    private boolean test(String text) {
        switch (mInputType) {
            case IP_ADDRESS: {
                int segments = 0;
                if (text.isEmpty()) {
                    return true;
                }
                if (text.charAt(0) == '.') {
                    return false;
                }
                if (text.contains("..")) {
                    return false;
                }
                try {
                    for (String part : text.split("\\.")) {
                        if (part.isEmpty()) {
                            return false;
                        }
                        int segment = Integer.parseInt(part);
                        segments++;
                        if (segments > 4) {
                            return false;
                        }
                        if (!(0 <= segment && segment <= 255)) {
                            return false;
                        }
                    }
                } catch (NumberFormatException e) {
                    return false;
                }

                if (segments == 4 && text.endsWith(".")) {
                    return false;
                }
                return true;
            }
            case USERNAME: {
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if ('A' <= c && c <= 'Z') {
                        // OK
                    }
                    else if ('a' <= c && c <= 'z') {
                        // OK
                    }
                    else if ('0' <= c && c <= '9') {
                        // OK
                    }
                    else if (c == '_') {
                        // OK
                    }
                    else {
                        return false;
                    }
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (test(sb.toString())) {
            super.replace(fb, offset, length, text, attrs);
        }
        else {
            // warn the user and don't allow the insert
        }

    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (test(sb.toString())) {
            super.remove(fb, offset, length);
        }
        else {
            // warn the user and don't allow the insert
        }

    }
}