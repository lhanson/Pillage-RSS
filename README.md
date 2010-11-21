Pillage
=======

What
----
Pillage is an RSS filter. In short, you can use it to clean up or modify your
RSS feeds so that they no longer include certain content, and maybe other neat
things.

Why
---
Here's an example (and actually the one which inspired this project): suppose
you subscribe to an RSS feed (from Tumblr, say) which contains a bunch of cool
images but is also being overrun by a lot of irritating banter between the
self-centered author and their adoring, creepy fans. You just want the image
posts, not the one-liners and "OMG gurl what kind of eyeliner do u use?" posts.
Solution: add the feed to your Pillage account and set it up to exclude text-only
items, then unsubscribe from the original feed in your feed reader and subscribe
to the Pillaged version.

How
---
Pillage is written in [Clojure](http://clojure.org/) using the
[Compojure](https://github.com/weavejester/compojure/wiki) web framework and
[Hiccup](https://github.com/weavejester/hiccup) for markup generation. It runs
on [Google App Engine](http://code.google.com/appengine/) using the
[appengine-clj](https://github.com/r0man/appengine-clj) Clojure library.

Clojure because I'm currently learning (and loving) it, Compojure because it's
pretty minimal (basically a wrapper around Ring with a nice routing syntax),
appengine-clj for nice Clojure bindings to the App Engine Java API,
and hiccup because I wanted to experiment with generating HTML entirely in code
(which much is nicer in a homoiconic language like Clojure than, say, Java)
and get away from ugly templates for a bit. We'll see how that shakes out...

## License

Copyright 2010 Lyle Hanson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
