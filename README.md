# OldSchoolGitHub

Long Story short = 
Pattern B by Virgil Dobjanschi Google IO 2010 (Content Provider API)

Loads dynamically (on scroll) data using CursorLoader to RecyclerView from GitHub users API through Retrofit2.
CursorLoader loads data from ContentProvider
Saves all pictures to cache and relevant data to SQLite db with Glide.
Processor, ServiceHelper and JobIntentService from API 26 also included
