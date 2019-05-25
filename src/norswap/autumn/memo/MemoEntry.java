package norswap.autumn.memo;

import norswap.autumn.LineMap;
import norswap.autumn.Parser;
import norswap.autumn.SideEffect;
import norswap.autumn.parsers.Memo;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Memoizer} entry, indicating a match over a range of the input, or a failure to match a
 * token at a given position.
 *
 * <p>Such entries are generated by a {@link Memo} parser or by a user-defined custom parser.
 *
 * <p>A failure to match is a valid entry, characterized by a -1 {@link #end_position} and an empty
 * {@link #delta}.
 */
public final class MemoEntry
{
    // ---------------------------------------------------------------------------------------------

    /** The parser that generated this result. */
    public final Parser parser;

    /** The start position of the match. */
    public final int start_position;

    /** The end position of the match. */
    public final int end_position;

    /** List of side-effects generated by the match. */
    public final List<SideEffect> delta;

    /** User-defined contextual information. */
    public final Object ctx;

    // ---------------------------------------------------------------------------------------------

    /**
     * Builds a new memo entry with the given parameters. {@code success} indicates whether the
     * parser succeeded. If false, the end position is overwritten to -1 and the delta is
     * overwritten to an empty list.
     */
    public MemoEntry (
        boolean success, Parser parser, int start_position, int end_position,
        List<SideEffect> delta, Object ctx)
    {
        this.parser = parser;
        this.start_position = start_position;
        this.end_position = success ? end_position : -1;
        this.delta = success ? delta : Collections.emptyList();
        this.ctx = ctx;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff the entry indicates a successful parser invocation.
     */
    public boolean succeeded()
    {
        return end_position > 0;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether this entry matches the passed parameters: same starting position, same
     * parser if {@code matcher_parser} is true and same context (may be null).
     */
    public boolean matches (boolean match_parser, Parser parser, int start_position, Object ctx)
    {
        return this.start_position == start_position
            && (!match_parser || this.parser == parser)
            && Objects.equals(this.ctx, ctx);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the entry, converting the position using {@code map} (can
     * be null, in which case plain offsets will be used).
     *
     * <p>Compared to {@link #toString(LineMap)}, this generates entries that look good in a dump of
     * a memoization table. This omits the class name, the hash; and the parser names if {@code
     * parser_name} is false.
     */
    public String listing_string (LineMap map, boolean parser_name)
    {
        String start = LineMap.string(map, start_position);

        if (!succeeded())
            return "at " + start + ": no match";

        StringBuilder b = new StringBuilder(128);

        b   .append("from ")    .append(start)
            .append(" to ")     .append(LineMap.string(map, end_position));

        if (parser_name)
            b.append(": ").append(parser);

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the entry, converting the position using {@code map} (can
     * be null, in which case plain offsets will be used).
     */
    public String toString (LineMap map)
    {
        StringBuilder b = new StringBuilder(128);

        b   .append("MemoEntry {")
            .append("{ parser = ") .append(parser)
            .append(", ");

        if (succeeded())
            b   .append("range = [")
                .append(LineMap.string(map, start_position))
                .append(" - ")
                .append(LineMap.string(map, end_position))
                .append("]");
        else
            b   .append("no match");

        b.append(" }");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString () {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}