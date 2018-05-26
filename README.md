# MelonScore
<a href="https://play.google.com/store/apps/details?id=vladyslavpohrebniakov.notgood"><img src="https://i.imgur.com/Vub9kwE.png" width="25%"/></a>

This is unofficial app that shows how <a href="https://www.youtube.com/theneedledrop">theneedledrop</a> rated album you're currently listening. Ratings are pulled from <a href="https://docs.google.com/spreadsheets/d/1GbGyWVtePH8RZCZd7N3RPDh8m-K6hgO6AyKsAHZpbeQ/edit?usp=drivesdk">Google Spreadsheets document</a>.
  
App downloads Google Spreadsheets document as `csv` file and saves data in database. It gets currently playig track info via Broadcast Receiver and showing rating for album. Also it uses Last.fm API to load album art.

# Screenshots
<a href="https://i.imgur.com/FORAp4t.png"><img src="https://i.imgur.com/FORAp4t.png" width="20%"/></a>
<a href="https://i.imgur.com/MKZbDzz.png"><img src="https://i.imgur.com/MKZbDzz.png" width="20%"/></a>
<a href="https://i.imgur.com/XmE0EFB.png"><img src="https://i.imgur.com/XmE0EFB.png" width="20%"/></a>
<a href="https://i.imgur.com/Iv7AQfD.png"><img src="https://i.imgur.com/Iv7AQfD.png" width="20%"/></a>

# Libraries
<a href="http://opencsv.sourceforge.net/">OpenCSV</a></br>
<a href="https://github.com/Kotlin/anko">anko</a></br>
<a href="http://square.github.io/okhttp">OkHttp</a></br>
<a href="http://square.github.io/picasso/">Picasso</a></br>

# License
```   Copyright 2018 Vladyslav Pohrebniakov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.```
