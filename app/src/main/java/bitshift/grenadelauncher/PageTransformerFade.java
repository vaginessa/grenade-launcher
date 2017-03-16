package bitshift.grenadelauncher;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

/**
 * Created by Bronson on 7/06/13.
 */
public class PageTransformerFade implements ViewPager.PageTransformer
{
    private static float ALPHA_SCALE = 1.75f; // 1 = default, 2 - fades quicker, 0 = fade slower
    private static float TRANSLATION_SCALE = 0.5f; // 0 = default, 1 = no scroll

    public void transformPage(View view, float position)
    {
        int pageWidth = view.getWidth();
        if (position <= -1)
        {
            // -1 = left pane
            view.setVisibility(View.GONE);
        }
        else if (position >= 1)
        {
            // +1 = right pane
            view.setVisibility(View.GONE);
        }
        else
        {
            // [-1,1] 0 = visible page!
            float pos = Math.abs(position);
            float alpha = 1 - (pos * ALPHA_SCALE); // Alpha 1 = center, alpha 0 = edge

            if (alpha <= 0f)
                view.setVisibility(View.GONE);
            else
            {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(alpha);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position * TRANSLATION_SCALE);
            }
        }



    }

}
