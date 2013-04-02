package weather;

import java.util.Comparator;

/**
 * <p>A comparator that emulates the "intuitive" sorting used by Windows
 * Explorer.  The rules are as follows:</p>
 * <p/>
 * <ul><li>Any sequence of one or more digits is treated as an atomic unit, a
 * number.  When these number units are matched up, they're compared according
 * to their respective numeric values.  If they're numerically equal, but one
 * has more leading zeroes than the other, the longer sequence is considered
 * smaller.</li>
 * <li>Numbers always sort before any other kind of character.</li>
 * <li>Space and punctuation characters always sort before letters; otherwise,
 * they sort according to their ASCII values.</li>
 * <li>Letters are sorted case-insensitively.</li></ul>
 * <p/>
 * <p>While Windows Explorer is capable of sorting filenames with accented Latin
 * letters as well as letters and numeric characters from many other languages,
 * this comparator will only work with strings of seven-bit ASCII characters.</p>
 * <p/>
 * <p>This class may be copied an distributed freely.</p>
 *
 * @author Alan Moore
 */
