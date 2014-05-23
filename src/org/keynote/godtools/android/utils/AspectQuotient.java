/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.keynote.godtools.android.utils;

import java.util.Observable;

/**
 * Class that holds the aspect quotient, defined as content aspect ratio divided
 * by view aspect ratio.
 */
public class AspectQuotient extends Observable {

    /**
     * Aspect quotient
     */
    private float mAspectQuotient;

    // Public methods

    /**
     * Gets aspect quotient
     * 
     * @return The aspect quotient
     */
    public float get() {
        return mAspectQuotient;
    }

    /**
     * Updates and recalculates aspect quotient based on supplied view and
     * content dimensions.
     * 
     * @param viewWidth Width of view
     * @param viewHeight Height of view
     * @param contentWidth Width of content
     * @param contentHeight Height of content
     */
    public void updateAspectQuotient(float viewWidth, float viewHeight, float contentWidth,
            float contentHeight) {
        final float aspectQuotient = (contentWidth / contentHeight) / (viewWidth / viewHeight);

        if (aspectQuotient != mAspectQuotient) {
            mAspectQuotient = aspectQuotient;
            setChanged();
        }
    }

}
