package org.cru.godtools.adapter

class EmptyListHeaderFooterAdapter internal constructor(builder: Builder) : BaseHeaderFooterAdapter(builder) {
    class Builder : BaseHeaderFooterAdapter.Builder<Builder>() {
        fun build(): EmptyListHeaderFooterAdapter {
            return EmptyListHeaderFooterAdapter(this)
        }
    }
}
