package uk.ac.ox.cs.diadem.oxpath.model.language.functions;

import java.util.Arrays;

/**
 *
 * @see org.apache.lucene.search.spell.JaroWinklerDistance
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class JaroWinklerDistance {

  private float threshold = 0.7f;

  private int[] matches(final String s1, final String s2) {
    String max, min;
    if (s1.length() > s2.length()) {
      max = s1;
      min = s2;
    } else {
      max = s2;
      min = s1;
    }
    final int range = Math.max((max.length() / 2) - 1, 0);
    final int[] matchIndexes = new int[min.length()];
    Arrays.fill(matchIndexes, -1);
    final boolean[] matchFlags = new boolean[max.length()];
    int matches = 0;
    for (int mi = 0; mi < min.length(); mi++) {
      final char c1 = min.charAt(mi);
      for (int xi = Math.max(mi - range, 0), xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
        if (!matchFlags[xi] && (c1 == max.charAt(xi))) {
          matchIndexes[mi] = xi;
          matchFlags[xi] = true;
          matches++;
          break;
        }
      }
    }
    final char[] ms1 = new char[matches];
    final char[] ms2 = new char[matches];
    for (int i = 0, si = 0; i < min.length(); i++) {
      if (matchIndexes[i] != -1) {
        ms1[si] = min.charAt(i);
        si++;
      }
    }
    for (int i = 0, si = 0; i < max.length(); i++) {
      if (matchFlags[i]) {
        ms2[si] = max.charAt(i);
        si++;
      }
    }
    int transpositions = 0;
    for (int mi = 0; mi < ms1.length; mi++) {
      if (ms1[mi] != ms2[mi]) {
        transpositions++;
      }
    }
    int prefix = 0;
    for (int mi = 0; mi < min.length(); mi++) {
      if (s1.charAt(mi) == s2.charAt(mi)) {
        prefix++;
      } else {
        break;
      }
    }
    return new int[] { matches, transpositions / 2, prefix, max.length() };
  }

  public float getDistance(final String s1, final String s2) {
    final int[] mtp = matches(s1, s2);
    final float m = mtp[0];
    if (m == 0)
      return 0f;
    final float j = (((m / s1.length()) + (m / s2.length()) + ((m - mtp[1]) / m))) / 3;
    final float jw = j < getThreshold() ? j : j + (Math.min(0.1f, 1f / mtp[3]) * mtp[2] * (1 - j));
    return jw;
  }

  /**
   * Sets the threshold used to determine when Winkler bonus should be used. Set to a negative value to get the Jaro
   * distance.
   *
   * @param threshold
   *          the new value of the threshold
   */
  public void setThreshold(final float threshold) {
    this.threshold = threshold;
  }

  /**
   * Returns the current value of the threshold used for adding the Winkler bonus. The default value is 0.7.
   *
   * @return the current value of the threshold
   */
  public float getThreshold() {
    return threshold;
  }
}