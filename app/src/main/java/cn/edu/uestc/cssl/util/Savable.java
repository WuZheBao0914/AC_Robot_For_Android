package cn.edu.uestc.cssl.util;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Interface for objects that can be loaded and saved with Bundles.
 *
 * @author xuyang
 * @create 2019/1/15 11:50
 **/
public interface Savable {
    /**
     * Load from a Bundle.
     *
     * @param bundle The Bundle
     */
    void load(@NonNull Bundle bundle);

    /**
     * Save to a Bundle.
     *
     * @param bundle The Bundle
     */
    void save(@NonNull Bundle bundle);
}
