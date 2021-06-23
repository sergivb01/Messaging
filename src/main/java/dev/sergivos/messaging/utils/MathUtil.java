package dev.sergivos.messaging.utils;

public enum MathUtil {
    ;
    private static final int QUICKSORT_NO_REC = 16;
    private static final int QUICKSORT_MEDIAN_OF_9 = 128;

    public static int percentile(int[] list, double percentile) {
        quickSort(list);
        int index = (int) Math.ceil(percentile / (list.length * 100));
        return list[index - 1];
    }

    /**
     * Credits: fastutils
     */

    /**
     * Sorts an array using quicksort
     *
     * @param x The array to sort
     */
    public static void quickSort(final int[] x) {
        quickSort(x, 0, x.length);
    }

    private static void quickSort(final int[] x, final int from, final int to) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if(len < QUICKSORT_NO_REC) {
            selectionSort(x, from, to);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if(len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s);
            m = med3(x, m - s, m, m + s);
            n = med3(x, n - 2 * s, n - s, n);
        }
        m = med3(x, l, m, n); // Mid-size, med of 3
        final int v = x[m];
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while(true) {
            int comparison;
            while(b <= c && (comparison = (Integer.compare((x[b]), (v)))) <= 0) {
                if(comparison == 0) swap(x, a++, b);
                b++;
            }
            while(c >= b && (comparison = (Integer.compare((x[c]), (v)))) >= 0) {
                if(comparison == 0) swap(x, c, d--);
                c--;
            }
            if(b > c) break;
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(x, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(x, b, to - s, s);
        // Recursively sort non-partition-elements
        if((s = b - a) > 1) quickSort(x, from, from + s);
        if((s = d - c) > 1) quickSort(x, to - s, to);
    }

    public static void selectionSort(final int[] a, final int from, final int to) {
        for(int i = from; i < to - 1; i++) {
            int m = i;
            for(int j = i + 1; j < to; j++) if(((a[j]) < (a[m]))) m = j;
            if(m != i) {
                final int u = a[i];
                a[i] = a[m];
                a[m] = u;
            }
        }
    }

    private static int med3(final int[] x, final int a, final int b, final int c) {
        final int ab = (Integer.compare((x[a]), (x[b])));
        final int ac = (Integer.compare((x[a]), (x[c])));
        final int bc = (Integer.compare((x[b]), (x[c])));
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    private static void swap(final int[] x, int a, int b, final int n) {
        for(int i = 0; i < n; i++, a++, b++) swap(x, a, b);
    }

    private static void swap(final int[] x, final int a, final int b) {
        final int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

}
