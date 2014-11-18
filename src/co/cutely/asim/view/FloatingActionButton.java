package co.cutely.asim.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import co.cutely.asim.R;

/**
 * A Floating Action Button implemented for pre-Lollipop niceness
 */
public class FloatingActionButton extends RelativeLayout {
	public FloatingActionButton(final Context context) {
		super(context);
		init(null);
	}

	public FloatingActionButton(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public FloatingActionButton(final Context context, final AttributeSet attrs,
			final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public FloatingActionButton(final Context context, final AttributeSet attrs,
			final int defStyleAttr,
			final int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);
	}

	protected void init(final AttributeSet attrs) {
		inflate(getContext(), R.layout.floating_action_button, this);

		// Do we have stying to do?
		if (attrs == null) {
			return;
		}

		final ImageView action = (ImageView) findViewById(R.id.action);
		final ImageView background = (ImageView) findViewById(R.id.background);

		// Get our attributes
		final TypedArray typedArray = getContext()
				.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton);
		for (int i = typedArray.getIndexCount() - 1; i >= 0; --i) {
			int attr = typedArray.getIndex(i);
			switch (attr) {
				case R.styleable.FloatingActionButton_actionDrawable:
					action.setImageDrawable(typedArray.getDrawable(i));
					break;
				case R.styleable.FloatingActionButton_color:
					final int color = typedArray.getInt(i, android.R.color.transparent);
					final GradientDrawable drawable = (GradientDrawable) background.getDrawable();
					drawable.setColor(color);
					break;
			}
		}

		// Cleanup
		typedArray.recycle();
	}
}
