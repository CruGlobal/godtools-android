#protect the SearchView init methods to ensure the Language filtering logic works
-keep class android.support.v7.widget.SearchView { <init>(...); }
