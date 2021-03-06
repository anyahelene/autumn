package norswap.autumn.memo;

import norswap.autumn.LineMap;
import norswap.autumn.Parser;
import norswap.utils.NArrays;
import norswap.utils.Strings;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

/**
 * A {@link Memoizer} implementation that memoizes the last {@code n} results it is passed.
 *
 * <p>The cache has two mode of operations depending on its {@link #match_parser} parameter. If
 * true, it will take into account the parser when storing/retrieving entries — otherwise it will
 * only take into account the input position and the optional context object.
 */
public final class MemoCache implements Memoizer
{
    // ---------------------------------------------------------------------------------------------

    private final int[] hashes;

    private final MemoEntry[] entries;

    private int next = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * The number of slots in this cache.
     */
    public final int num_slots;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether queries to the table should check the parser when returning an entry, or just
     * the start position.
     */
    public final boolean match_parser;

    // ---------------------------------------------------------------------------------------------

    public MemoCache (int num_slots, boolean match_parser)
    {
        this.num_slots = num_slots;
        this.match_parser = match_parser;
        this.entries = new MemoEntry[num_slots];
        this.hashes = new int[num_slots];
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void memoize (MemoEntry entry)
    {
        // fills next slot (unoccupied or oldest added)
        hashes[next] = Memoizer.hash(match_parser, entry);
        entries[next] = entry;
        if (++next == num_slots) next = 0;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public MemoEntry get (Parser parser, int pos, Object ctx)
    {
        int hash = Memoizer.hash(match_parser, parser, pos, ctx);

        // iterate over slots from the most recently to least recently added
        for (int i = 0; i < num_slots; ++i)
        {
            int j = next - 1 - i;
            if (j < 0) j += num_slots;
            if (hashes[j] == 0)
                return null;
            if (hashes[j] == hash && entries[j].matches(match_parser, parser, pos, ctx))
                return entries[j];
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private String string (String sep, Function<MemoEntry, String> f)
    {
        MemoEntry[] entries = this.entries.clone();
        Arrays.sort(entries, Comparator.comparingInt(x -> x.start_position));
        StringBuilder b = new StringBuilder();
        Strings.separated(b, sep, NArrays.map(entries, new String[0], f));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString (LineMap map)
    {
        return "MemoCache { " + string(", ", e -> e.toString(map)) + "}";
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String listing (LineMap map)
    {
        return string("\n", e -> e.listing_string(map, match_parser));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}
