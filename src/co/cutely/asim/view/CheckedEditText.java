package co.cutely.asim.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import co.cutely.asim.R;

/**
 * An EditText that can be validated
 *
 */
@SuppressWarnings("unused")
public class CheckedEditText extends EditText implements View.OnFocusChangeListener,
		TextView.OnEditorActionListener {

	public static final String TAG = CheckedEditText.class.getSimpleName();

	private Drawable warning;

	private boolean validated;

	private Validator validator;

	private OnFocusChangeListener focusChangeListener;
	private OnEditorActionListener editorActionListener;

	public CheckedEditText(final Context context) {
		super(context);
	}

	public CheckedEditText(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CheckedEditText(final Context context, final AttributeSet attrs,
			final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CheckedEditText(final Context context, final AttributeSet attrs, final int defStyleAttr,
			final int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	/**
	 * Checks if the text currently entered is valid according to the supplied validator
	 *
	 * @return {@code true} if the text is valid, {@code false} otherwise
	 */
	public boolean isValid() {
		return validator == null || validator.isValid(getText().toString());
	}

	public String getValue() {
		return getText() != null ? getText().toString() : null;
	}

	/**
	 * Sets a validator for the EditText
	 *
	 * @param validator The validator to use for this input
	 */
	public void setValidator(final Validator validator) {
		this.validator = validator;
	}

	@Override
	public void setOnFocusChangeListener(final OnFocusChangeListener l) {
		focusChangeListener = l;
	}

	@Override
	public void setOnEditorActionListener(OnEditorActionListener l) {
		editorActionListener = l;
	}

	@Override
	public void onFocusChange(final View v, final boolean hasFocus) {
		// Only check on focus loss
		if (!hasFocus && validator != null) {
			validated = validator.isValid(getText().toString());

			final Drawable indicator = (validated) ? null : warning;
			setCompoundDrawablesWithIntrinsicBounds(null, null, indicator, null);
		}

		// Pass along the event if there's another listener
		if (focusChangeListener != null) {
			focusChangeListener.onFocusChange(v, hasFocus);
		}
	}

	protected void init() {
		warning = getResources().getDrawable(R.drawable.ic_warning_grey600_18dp);

		// Listen for focus change events for validation
		super.setOnFocusChangeListener(this);
		super.setOnEditorActionListener(this);
	}

	@Override
	public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
		// Validate on an action
		if (validator != null) {
			validated = validator.isValid(getText().toString());
		}

		// Pass it along if there's another listener
		return editorActionListener != null &&
				editorActionListener.onEditorAction(v, actionId, event);
	}

	/**
	 * Filter for validating a CheckedEditText's value
	 */
	public interface Validator {
		/**
		 * Filter to check for the validity of a string
		 *
		 * @param content The contents of the CheckedEditText
		 * @return {@code true} if the contents are valid {@code false} otherwise
		 */
		boolean isValid(final String content);
	}
}
