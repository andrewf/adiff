# adiff

An implementation of associative patch composition, more or less
mathematically proven.

Everything basically works, but it's pretty inefficient, and if you actually know
Clojure and have a sensitive stomach, maybe you shouldn't look at the
code too closely. This is partly a learning project.

## Theory

### Motivation

There are two interesting ideas here. One is that all data is a patch, with
"plain" data interpreted as a patch against some null or empty object. The
second is that you should be able to treat a patch just like the data it
operates on. More specifically, if you have some data D and a patch P1 on D,
applying P1 to D and then applying P2 should be the same as applying P2 to
P1, then applying the resulting patch to D; from a patch perspective, P1
behaves the same regardless of whether or not it has been applied already.
In other words, patch application should be associative.

For example, suppose you have a document template, T, and some number of other
documents based on it. You represent each version of the document as a
patch against the template. We'll pick one and call it D. To actually work on the
document, you'll want to actually apply the patch to the template and work on the
result, (D*T). Your work is represented as a patch W, so the whole thing is
W*(D*T). To save your work, though, you take the same patch W and apply it to D,
and save that as the latest version of D: D = W*D. This only works if the
parentheses can be freely re-arranged.

The first idea is stolen straight out of Google Wave [1] (except simpler),
and I'm pretty sure the Wave team didn't invent it either. I haven't seen
the second idea put quite this way before. It's quite possible my
Google-fu is inadequate.

### Simple (non-nested) patches

Let's first consider patches consisting of simple lists of objects, of which only two are special, `%I` and `%D`. For example:

    [a b c d] * [] = [a b c d]

where `*` is patch composition. The `%I` (for "identity") token copies, or "reads" a symbol
from the RHS into the output.

    [a b %I c d] * [x] = [a b x c d]

The number of "reads" in the patch, that is the LHS, must be balanced with
the number of "writes" in the input, or RHS. We will soon have to develop
concepts of the "write" and "read" dimensions of a patch to express this.

The `%D` token deletes or skips elements from the input.

    [x y %D %I] * [a b] = [x y b]

`%D` reads a token from the input but does not write to the output; it has a read
dimension of 1 and a write dimension of 0. We can say that `%I` has write and
read dimensions of 1, and anything else has a read dimension of 0 and write
dimension of 1. We use `%` to indicate that some object has special behavior
that interacts with an input patch.

Now for patch dimensions: a simple patch's write and read dimensions are the
sum of the write and read dimensions of its elements. Any two patches where
the left patch's read dimension matches the left patch's write dimension can
be composed. So `[%D %D %I y %I]` has a write dimension of 3 and a read dimension
of 4. These dimensions are used much more in mathematically analyzing patches
than in actually evaluating them.

There is one other trick to make these patches associative. My first idea was
that the left patch would simply be a template for the output, but this does
not quite work. Naively,

    [x %I %D]*([%D %I %I]*[a b c]) = [x %I %D]*[b c] = [x b]
    ([x %I %D]*[%D %I %I])*[a b c] = [x %I]*[a b c] = dimensionally incompatible

The trick is to carry `%D`s in the right patch into the output. This makes sure
that whatever the `%D` would have deleted if we had composed from right to left
still gets deleted. Also, `%D*%I = %D` for similar reasons: the `%D` needs to delete
whatever the `%I` would have kept if it had been evaluated first. Re-evaluating the
second example above with the new rule, we get:

    ([x %I %D]*[%D %I %I])*[a b c] = [%D x %I %D]*[a b c] = [x b]

`%I*%D` is undefined because of dimensionality, except that `[%I]*[%D] = [%D %I]`.
The ordering of `%D` and `%I` is arbitrary in this situation. I picked it this way
because it seems to better express replacing some elements with others, the most
likely semantic situation to trigger the ambiguity.

So that's the result of about 3 days worth of obsessing over the problem. In
the following semi-formal definition, I'll write arbitrary sequences of symbols
within patches as `<...>`, and annotate them with their write and read
dimensions, in that order, like `<a b I D>:3,2` and `<...>:a,b` . Also, assume
`y` is any symbol with dimension 1,0, basically any non-special symbol.

    <y <...>:a,b> * <  <...>:b,c> = <y <...>:a,b * <...>:b,c>
    <D <...>:a,b> * <y <...>:b,c> = <  <...>:a,b * <...>:b,c>
    <D <...>:a,b> * <I <...>:b,c> = <  <...>:a,b * <...>:b,c>
    <  <...>:a,b> * <D <...>:b,c> = <D <...>:a,b * <...>:b,c>
    <I <...>:a,b> * <y <...>:b,c> = <y <...>:a,b * <...>:b,c>
    <I <...>:a,b> * <D <...>:b,c> = <D <...>:a,b * <...>:b,c>
    <I <...>:a,b> * <I <...>:b,c> = <I <...>:a,b * <...>:b,c>

This can be shown to be associative as long as the compositions of the rest
of the streams are also associative. To compose two lists of compatible
dimension, simply compose their contents by the above algorithm. 

### Nested patches

Streams of symbols are cool (that's all text is after all), but wouldn't it be
cooler if we could do this with more complex data structures, like nested
lists? With nested lists we can build interesting things like structured
documents, anything you can do in s-exprs, which is a lot.

A single list in a patch simply inserts itself in the output, just like a
regular object:

    [a b [c d e] %D %I] * [x y] = [a b [c d e] y]

Now we also want to be able to selectively modify parts of a list. To do so
we can make an entire list into a reader, represented like `%[...]` (or in Clojure code, `(% [...])`).

   [ %[a %D %D %I] %D [b c] ] * [ [x y z] [q r] ] = [ [a z] [b c] ]

Notice how in that example we also deleted a whole list in one shot with a `%D`
operator. Of course we can also keep a list in its entirety with an `%I`.

Here are the necessary extensions to the composition rules, neglecting the
"rest of the patch" gunk that made the previous definitions so long.

    <[LLL]> * <> = <[LLL]>
    <%I> * <[LLL]> = <[LLL]>
    <%I> * <%[LLL]> = <%[LLL]>
    <%[LLL]> * <%I> = <%[LLL]>
    <%D> * <[LLL]> = <>
    <%[LLL]> * <[RRR]> = <[LLL*RRR]>
    <%[LLL]> * <%[RRR]> = <%[LLL*RRR]>

Where LLL and RRR are sequences of elements.

Notice the difference between the last two. A patch composed with a list
returns a plain list; only when you compose two patches is the result a patch
that can be applied to something else. This is important for preserving
associativity. If `%[LLL] * [RRR] = %[LLL*RRR]` , then an expression that did not
originally read anything from an input patch has suddenly turned into a reader.
This will break associativity, even if it happens to work dimensionally,
because the resulting patch will attempt to modify something that should
have been read by another patch element.

### Ok, but what about real life?

If one was patching text, each character would be a token, and each D and I
would skip or retain a single character. One would obviously want to use
some kind of run-length encoding to make this more eficient.

[1] Specifically, this article: http://www.codecommit.com/blog/java/understanding-and-applying-operational-transformation

## Usage

Just call the adiff.core.compose function with two vectors that may or may
not contain any special patch operators, of compatible sizes. In principle
anything that supports (first) and (rest) should work. compose will raise UnsupportedOperationException
if the patches are incompatible.

## License

Copyright © 2013 Andrew Fleenor

Distributed under the MIT license.
