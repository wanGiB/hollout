/**
 * Copyright (c) 2016-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.wan.hollout.animations.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListHelper {

  /**
   * Sorts a list, if it exists.  Otherwise just returns.
   */
  public static void sort(List list, Comparator comparator) {
    if (list == null || list.isEmpty()) {
      return;
    }
    Collections.sort(list, comparator);
  }

  /**
   * Returns either an immutable copy of a list, or an empty list if the list is null.
   * @return An immutable copy of the list, guaranteed to be non-null.
   */
  public static List immutableOrEmpty(List list) {
    return list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(list);
  }
}
