package com.gelakinetic.NetDraftJ.Client;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

class TextInputFilter extends DocumentFilter {
    public enum inputType {
        USERNAME, IP_ADDRESS
    }

    private final inputType mInputType;

    /**
     * Create a new TextInputFilter for either the username or IP address
     * 
     * @param type
     *            The type of filter, either USERNAME or IP_ADDRESS
     */
    TextInputFilter(inputType type) {
        mInputType = type;
    }

    /**
     * Invoked prior to insertion of text into the specified Document. Subclasses that want to conditionally allow
     * insertion should override this and only call supers implementation as necessary, or call directly into the
     * FilterBypass.
     *
     * @param fb
     *            FilterBypass that can be used to mutate Document
     * @param offset
     *            the offset into the document to insert the content >= 0. All positions that track change at or after
     *            the given location will move.
     * @param string
     *            the string to insert
     * @param attr
     *            the attributes to associate with the inserted content. This may be null if there are no attributes.
     * @throws BadLocationException
     *             the given insert position is not a valid position within the document
     */
    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (validateText(sb.toString())) {
            super.insertString(fb, offset, string, attr);
        }
    }

    /**
     * Validate the given text making sure it's either a valid IP address in progress or username
     * 
     * @param text
     *            The text to validate
     * @return false if the text is illegal, true if it's valid
     */
    private boolean validateText(String text) {
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

                return segments != 4 || !text.endsWith(".");
            }
            case USERNAME: {
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (!('A' <= c && c <= 'Z') && !('a' <= c && c <= 'z') && !('0' <= c && c <= '9') && !(c == '_')) {
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

    /**
     * Invoked prior to replacing a region of text in the specified Document. Subclasses that want to conditionally
     * allow replace should override this and only call supers implementation as necessary, or call directly into the
     * FilterBypass.
     * 
     * @param fb
     *            FilterBypass that can be used to mutate Document
     * @param offset
     *            Location in Document
     * @param length
     *            Length of text to delete
     * @param text
     *            Text to insert, null indicates no text to insert
     * @param attributeSet
     *            AttributeSet indicating attributes of inserted text, null is legal.
     * @throws BadLocationException
     *             the given insert position is not a valid position within the document
     */
    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet)
            throws BadLocationException {

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (validateText(sb.toString())) {
            super.replace(fb, offset, length, text, attributeSet);
        }

    }

    /**
     * Invoked prior to removal of the specified region in the specified Document. Subclasses that want to conditionally
     * allow removal should override this and only call supers implementation as necessary, or call directly into the
     * FilterBypass as necessary.
     *
     * @param fb
     *            FilterBypass that can be used to mutate Document
     * @param offset
     *            the offset from the beginning >= 0
     * @param length
     *            the number of characters to remove >= 0
     * @throws BadLocationException
     *             some portion of the removal range was not a valid part of the document. The location in the exception
     *             is the first bad position encountered.
     */
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (validateText(sb.toString())) {
            super.remove(fb, offset, length);
        }

    }
}