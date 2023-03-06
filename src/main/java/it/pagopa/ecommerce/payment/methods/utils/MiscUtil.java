package it.pagopa.ecommerce.payment.methods.utils;

import java.util.List;

public class MiscUtil {

    public static <T> List<T> addAndGetList(
                                            List<T> list,
                                            T entry
    ) {
        list.add(entry);
        return list;
    }
}
