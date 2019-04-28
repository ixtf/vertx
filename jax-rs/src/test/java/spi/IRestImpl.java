package spi;

import com.google.inject.Singleton;

/**
 * @author jzb 2019-04-28
 */
@Singleton
public class IRestImpl implements IRest {
    @Override
    public String get() {
        return Thread.currentThread().toString();
    }
}
