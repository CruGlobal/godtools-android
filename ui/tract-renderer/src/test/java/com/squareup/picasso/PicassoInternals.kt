package com.squareup.picasso

var picassoSingleton: Picasso?
    get() = Picasso.singleton
    set(value) {
        Picasso.singleton = value
    }
