package com.hexidec.ekit.component;
/**
 * Copyright 11/7/15 by Stephen Beitzel
 */

import java.awt.*;
import java.util.NoSuchElementException;

/**
 * Interface specifying how an IMG tag's source might be resolved for use
 * by an HTMLEditorKit.
 *
 * @author Stephen Beitzel &lt;sbeitzel@pobox.com&gt;
 */
public interface ImageProvider {
    /**
     * Given a string from the src attribute on an img tag, try to find
     * a corresponding Image. If one could not be found, throw an exception.
     *
     * @param reference string defining where to find the image
     * @return the image
     * @throws NoSuchElementException if no corresponding image could be loaded
     */
    Image resolveImageReference(String reference) throws NoSuchElementException;
}
