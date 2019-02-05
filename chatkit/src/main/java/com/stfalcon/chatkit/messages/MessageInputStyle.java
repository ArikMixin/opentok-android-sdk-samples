/*******************************************************************************
 * Copyright 2016 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.stfalcon.chatkit.messages;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import com.stfalcon.chatkit.R;
import com.stfalcon.chatkit.commons.Style;

/**
 * Style for MessageInputStyle customization by xml attributes
 */
@SuppressWarnings("WeakerAccess")
class MessageInputStyle extends Style {

    private static final int DEFAULT_MAX_LINES = 5;
    private static final int DEFAULT_DELAY_TYPING_STATUS = 1500;

    private boolean showAttachmentButton;

    private int attachmentButtonBackground;
    private int attachmentButtonDefaultBgColor;
    private int attachmentButtonDefaultBgPressedColor;
    private int attachmentButtonDefaultBgDisabledColor;

    private int attachmentButtonIcon;
    private int attachmentButtonDefaultIconColor;
    private int attachmentButtonDefaultIconPressedColor;
    private int attachmentButtonDefaultIconDisabledColor;

    private int attachmentButtonWidth;
    private int attachmentButtonHeight;
    private int attachmentButtonMargin;

	private boolean showCameraButton;

	private int cameraButtonBackground;
	private int cameraButtonDefaultBgColor;
	private int cameraButtonDefaultBgPressedColor;
	private int cameraButtonDefaultBgDisabledColor;

	private int cameraButtonIcon;
	private int cameraButtonDefaultIconColor;
	private int cameraButtonDefaultIconPressedColor;
	private int cameraButtonDefaultIconDisabledColor;

	private int cameraButtonWidth;
	private int cameraButtonHeight;
	private int cameraButtonMargin;

	/*********************************************/
	private boolean showVideoButton;
	private boolean showLocationButton;
	private boolean showMagicButton;

	private int videoButtonIcon;
	private int locationButtonIcon;
	private int magicButtonIcon;

	/*********************************************/




	private int inputButtonBackground;
    private int inputButtonDefaultBgColor;
    private int inputButtonDefaultBgPressedColor;
    private int inputButtonDefaultBgDisabledColor;

    private int inputButtonIcon;
    private int inputButtonDefaultIconColor;
    private int inputButtonDefaultIconPressedColor;
    private int inputButtonDefaultIconDisabledColor;

    private int inputButtonWidth;
    private int inputButtonHeight;
    private int inputButtonMargin;

    private int inputMaxLines;
    private String inputHint;
    private String inputText;

    private int inputTextSize;
    private int inputTextColor;
    private int inputHintColor;

    private Drawable inputBackground;
    private Drawable inputCursorDrawable;

    private int inputDefaultPaddingLeft;
    private int inputDefaultPaddingRight;
    private int inputDefaultPaddingTop;
    private int inputDefaultPaddingBottom;

    private int delayTypingStatus;

    static MessageInputStyle parse(Context context, AttributeSet attrs) {
        MessageInputStyle style = new MessageInputStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MessageInput);

        style.showAttachmentButton = typedArray.getBoolean(R.styleable.MessageInput_showAttachmentButton, false);

        style.attachmentButtonBackground = typedArray.getResourceId(R.styleable.MessageInput_attachmentButtonBackground, -1);
        style.attachmentButtonDefaultBgColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultBgColor,
                style.getColor(R.color.white_four));
        style.attachmentButtonDefaultBgPressedColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultBgPressedColor,
                style.getColor(R.color.white_five));
        style.attachmentButtonDefaultBgDisabledColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultBgDisabledColor,
                style.getColor(R.color.transparent));

        style.attachmentButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_attachmentButtonIcon, -1);
        style.attachmentButtonDefaultIconColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultIconColor,
                style.getColor(R.color.cornflower_blue_two));
        style.attachmentButtonDefaultIconPressedColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultIconPressedColor,
                style.getColor(R.color.cornflower_blue_two_dark));
        style.attachmentButtonDefaultIconDisabledColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultIconDisabledColor,
                style.getColor(R.color.cornflower_blue_light_40));

        style.attachmentButtonWidth = typedArray.getDimensionPixelSize(R.styleable.MessageInput_attachmentButtonWidth, style.getDimension(R.dimen.input_button_width));
        style.attachmentButtonHeight = typedArray.getDimensionPixelSize(R.styleable.MessageInput_attachmentButtonHeight, style.getDimension(R.dimen.input_button_height));
        style.attachmentButtonMargin = typedArray.getDimensionPixelSize(R.styleable.MessageInput_attachmentButtonMargin, style.getDimension(R.dimen.input_button_margin));

/*****************************************************************/

		style.showCameraButton = typedArray.getBoolean(R.styleable.MessageInput_showAttachmentButton, false);

		style.cameraButtonBackground = typedArray.getResourceId(R.styleable.MessageInput_cameraButtonBackground, -1);
		style.cameraButtonDefaultBgColor = typedArray.getColor(R.styleable.MessageInput_cameraButtonDefaultBgColor,
			style.getColor(R.color.white_four));
		style.cameraButtonDefaultBgPressedColor = typedArray.getColor(R.styleable.MessageInput_cameraButtonDefaultBgPressedColor,
			style.getColor(R.color.white_five));
		style.cameraButtonDefaultBgDisabledColor = typedArray.getColor(R.styleable.MessageInput_cameraButtonDefaultBgDisabledColor,
			style.getColor(R.color.transparent));

		style.cameraButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_cameraButtonIcon, -1);
		style.cameraButtonDefaultIconColor = typedArray.getColor(R.styleable.MessageInput_cameraButtonDefaultIconColor,
			style.getColor(R.color.cornflower_blue_two));
		style.cameraButtonDefaultIconPressedColor = typedArray.getColor(R.styleable.MessageInput_cameraButtonDefaultIconPressedColor,
			style.getColor(R.color.cornflower_blue_two_dark));
		style.cameraButtonDefaultIconDisabledColor = typedArray.getColor(R.styleable.MessageInput_cameraButtonDefaultIconDisabledColor,
			style.getColor(R.color.cornflower_blue_light_40));

		style.cameraButtonWidth = typedArray.getDimensionPixelSize(R.styleable.MessageInput_cameraButtonWidth, style.getDimension(R.dimen.input_button_width));
		style.cameraButtonHeight = typedArray.getDimensionPixelSize(R.styleable.MessageInput_cameraButtonHeight, style.getDimension(R.dimen.input_button_height));
		style.cameraButtonMargin = typedArray.getDimensionPixelSize(R.styleable.MessageInput_cameraButtonMargin, style.getDimension(R.dimen.input_button_margin));
/****************************************************************************/

		style.showVideoButton = typedArray.getBoolean(R.styleable.MessageInput_showVideoButton, false);
		style.showLocationButton = typedArray.getBoolean(R.styleable.MessageInput_showLocationButton, false);
		style.showMagicButton = typedArray.getBoolean(R.styleable.MessageInput_showMagicButton, false);


		style.videoButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_videoButtonIcon, -1);
		style.locationButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_locationButtonIcon, -1);
		style.magicButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_magicButtonIcon, -1);
/****************************************************************************/



        style.inputButtonBackground = typedArray.getResourceId(R.styleable.MessageInput_inputButtonBackground, -1);
        style.inputButtonDefaultBgColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultBgColor,
                style.getColor(R.color.cornflower_blue_two));
        style.inputButtonDefaultBgPressedColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultBgPressedColor,
                style.getColor(R.color.cornflower_blue_two_dark));
        style.inputButtonDefaultBgDisabledColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultBgDisabledColor,
                style.getColor(R.color.white_four));

        style.inputButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_inputButtonIcon, -1);
        style.inputButtonDefaultIconColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultIconColor,
                style.getColor(R.color.white));
        style.inputButtonDefaultIconPressedColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultIconPressedColor,
                style.getColor(R.color.white));
        style.inputButtonDefaultIconDisabledColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultIconDisabledColor,
                style.getColor(R.color.warm_grey));

        style.inputButtonWidth = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputButtonWidth, style.getDimension(R.dimen.input_button_width));
        style.inputButtonHeight = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputButtonHeight, style.getDimension(R.dimen.input_button_height));
        style.inputButtonMargin = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputButtonMargin, style.getDimension(R.dimen.input_button_margin));

        style.inputMaxLines = typedArray.getInt(R.styleable.MessageInput_inputMaxLines, DEFAULT_MAX_LINES);
        style.inputHint = typedArray.getString(R.styleable.MessageInput_inputHint);
        style.inputText = typedArray.getString(R.styleable.MessageInput_inputText);

        style.inputTextSize = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputTextSize, style.getDimension(R.dimen.input_text_size));
        style.inputTextColor = typedArray.getColor(R.styleable.MessageInput_inputTextColor, style.getColor(R.color.dark_grey_two));
        style.inputHintColor = typedArray.getColor(R.styleable.MessageInput_inputHintColor, style.getColor(R.color.warm_grey_three));

        style.inputBackground = typedArray.getDrawable(R.styleable.MessageInput_inputBackground);
        style.inputCursorDrawable = typedArray.getDrawable(R.styleable.MessageInput_inputCursorDrawable);

        style.delayTypingStatus = typedArray.getInt(R.styleable.MessageInput_delayTypingStatus, DEFAULT_DELAY_TYPING_STATUS);

        typedArray.recycle();

        style.inputDefaultPaddingLeft = style.getDimension(R.dimen.input_padding_left);
        style.inputDefaultPaddingRight = style.getDimension(R.dimen.input_padding_right);
        style.inputDefaultPaddingTop = style.getDimension(R.dimen.input_padding_top);
        style.inputDefaultPaddingBottom = style.getDimension(R.dimen.input_padding_bottom);

        return style;
    }

    private MessageInputStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Drawable getSelector(@ColorInt int normalColor, @ColorInt int pressedColor,
                                 @ColorInt int disabledColor, @DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_enabled, -android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_enabled}
                        },
                        new int[]{normalColor, pressedColor, disabledColor}
                ));
        return drawable;
    }

    protected boolean showAttachmentButton() {
        return showAttachmentButton;
    }

    protected Drawable getAttachmentButtonBackground() {
        if (attachmentButtonBackground == -1) {
            return getSelector(attachmentButtonDefaultBgColor, attachmentButtonDefaultBgPressedColor,
                    attachmentButtonDefaultBgDisabledColor, R.drawable.mask);
        } else {
            return getDrawable(attachmentButtonBackground);
        }
    }

    protected Drawable getAttachmentButtonIcon() {
        if (attachmentButtonIcon == -1) {
            return getSelector(attachmentButtonDefaultIconColor, attachmentButtonDefaultIconPressedColor,
                    attachmentButtonDefaultIconDisabledColor, R.drawable.ic_add_attachment);
        } else {
            return getDrawable(attachmentButtonIcon);
        }
    }

    protected int getAttachmentButtonWidth() {
        return attachmentButtonWidth;
    }

    protected int getAttachmentButtonHeight() {
        return attachmentButtonHeight;
    }

    protected int getAttachmentButtonMargin() {
        return attachmentButtonMargin;
    }




	protected boolean showCameraButton() {
		return showCameraButton;
	}

	protected Drawable getCameraButtonBackground() {
		if (cameraButtonBackground == -1) {
			return getSelector(cameraButtonDefaultBgColor, cameraButtonDefaultBgPressedColor,
				cameraButtonDefaultBgDisabledColor, R.drawable.mask);
		} else {
			return getDrawable(cameraButtonBackground);
		}
	}

	protected Drawable getCameraButtonIcon() {
		if (cameraButtonIcon == -1) {
			return getSelector(cameraButtonDefaultIconColor, cameraButtonDefaultIconPressedColor,
				cameraButtonDefaultIconDisabledColor, R.drawable.ic_add_attachment);
		} else {
			return getDrawable(cameraButtonIcon);
		}
	}

	protected int getCameraButtonWidth() {
		return cameraButtonWidth;
	}

	protected int getCameraButtonHeight() {
		return cameraButtonHeight;
	}

	protected int getCameraButtonMargin() {
		return cameraButtonMargin;
	}


	protected boolean showVideoButton() {
		return showVideoButton;
	}
	protected boolean showLocationButton() {
		return showLocationButton;
	}
	protected boolean showMagicButton() {
		return showMagicButton;
	}



	protected Drawable getMagicButtonIcon() {
		return getDrawable(magicButtonIcon);
	}
	protected Drawable getLocationButtonIcon() {
		return getDrawable(locationButtonIcon);
	}
	protected Drawable getVideoButtonIcon() {
		return getDrawable(videoButtonIcon);
	}

    protected Drawable getInputButtonBackground() {
        if (inputButtonBackground == -1) {
            return getSelector(inputButtonDefaultBgColor, inputButtonDefaultBgPressedColor,
                    inputButtonDefaultBgDisabledColor, R.drawable.mask);
        } else {
            return getDrawable(inputButtonBackground);
        }
    }

    protected Drawable getInputButtonIcon() {
        if (inputButtonIcon == -1) {
            return getSelector(inputButtonDefaultIconColor, inputButtonDefaultIconPressedColor,
                    inputButtonDefaultIconDisabledColor, R.drawable.ic_send);
        } else {
            return getDrawable(inputButtonIcon);
        }
    }

    protected int getInputButtonMargin() {
        return inputButtonMargin;
    }

    protected int getInputButtonWidth() {
        return inputButtonWidth;
    }

    protected int getInputButtonHeight() {
        return inputButtonHeight;
    }

    protected int getInputMaxLines() {
        return inputMaxLines;
    }

    protected String getInputHint() {
        return inputHint;
    }

    protected String getInputText() {
        return inputText;
    }

    protected int getInputTextSize() {
        return inputTextSize;
    }

    protected int getInputTextColor() {
        return inputTextColor;
    }

    protected int getInputHintColor() {
        return inputHintColor;
    }

    protected Drawable getInputBackground() {
        return inputBackground;
    }

    protected Drawable getInputCursorDrawable() {
        return inputCursorDrawable;
    }

    protected int getInputDefaultPaddingLeft() {
        return inputDefaultPaddingLeft;
    }

    protected int getInputDefaultPaddingRight() {
        return inputDefaultPaddingRight;
    }

    protected int getInputDefaultPaddingTop() {
        return inputDefaultPaddingTop;
    }

    protected int getInputDefaultPaddingBottom() {
        return inputDefaultPaddingBottom;
    }

    int getDelayTypingStatus() {
        return delayTypingStatus;
    }

}
