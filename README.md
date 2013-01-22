# adiff

An implementation of associative patch composition, more or less mathematically proven.

## Theory

There are two interesting ideas here. One is that all data is a patch, with "plain" data interpreted as a patch against some null or empty object. The second is that you should be able to treat a patch just like the data it operates on. More specifically, if you have some data D and a patch P1 on D, applying P1 to D and then applying P2 should be the same as applying P2 to P1, then applying the resulting patch to D; in other words, patch application should be associative.

The first is stolen straight out of Google Wave [1] (except simpler), and I'm pretty sure the Wave team didn't invent it either. I haven't seen the second idea put quite this way before.

For now, I represent patches as simple lists of tokens, symbols mostly in this
implementation, of which only two are special, I and D. For example:

    [a b c d] * [] = [a b c d]

where * is patch composition. The I token copies, or "reads" a symbol
from the RHS into the output.

    [a b I c d] * [x] = [a b x c d]

The number of "reads" in the patch, that is the LHS, must be balanced with the number of
"writes" in the input, or RHS. We will soon have to develop concepts of the "write"
and "read" dimensions of a patch to express this.

The D token deletes or skips elements from the input.

    [x y D I] * [a b] = [x y b]

D reads a token from the input but does not write to the output; it has a read
dimension of 1 and a write dimension of 0. We can say that I has write and
read dimensions of 1, and anything else has a read dimension of 0 and write
dimension of 1.

Now for patch dimensions: a full patch's write and read dimensions are the sum of the
write and read dimensions of its elements. Any two patches where the left
patch's read dimension matches the left patch's write dimension can be composed. So:

    

There is one other trick to make these patches associative. My first idea was
that the left patch would simply be a template for the output, but this does
not quite work. Naively,

    [x I D]*([D I I]*[a b c]) = [x I D]*[b c] = [x b]
    ([x I D]*[D I I])*[a b c] = [x I]*[a b c] = dimensionally incompatible

The trick is to carry D's in the right patch into the output. This makes sure
that whatever the D would have deleted if we had composed from right to left
still gets deleted. Also, D*I = D for similar reasons: the D needs to delete
whatever the I would have kept if it had been evaluated first. And I*D is
obviously D as well.

So that's the result of about 3 days worth of obsessing over the problem. In the following
semi-formal definition, I'll write arbitrary sequences of symbols within
patches as <...>, and annotate them with their dimension, like <a b D D>:2,2
and <...>:a,b . Also, assume y is any symbol with dimension 1,0, basically any non-special symbol.

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

### Ok, but what about real life?

If one was patching text, each character would be a token, and each D and I
would skip or retain a single character. One would obviously want to use
some kind of run-length encoding to make this more eficient.

I haven't done the math or implemented it yet, but I belive this should
generalize fairly smoothly to nested lists, which make an expressive data
model (i.e, s-expressions).

[1] Specifically, this article: http://www.codecommit.com/blog/java/understanding-and-applying-operational-transformation

## Usage

Just call the adiff.core.compose function with two lists or vectors or what have you, anything that supports (first) and (rest), of compatible sizes, like (compose '(a b D I) '(x y)). It will raise UnsupportedOperationException if the patches are incompatible.

## License

Copyright © 2013 Andrew Fleenor

Distributed under the MIT license.
