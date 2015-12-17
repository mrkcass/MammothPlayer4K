# MammothPlayer4K

Purpose:
  *Hardware accelerated video player for looping 4K, kiosk style, videos.
  *Native code based on Android NdkMediaCodec.
  *Created and tested on a NVidia Shield TV. I'm sure it will run on other platforms but they are not tested.

Notes:
  *Hardware accelerated h.265 playback
  *Movies are loaded from the sdcard. the default directory is /sdcard/aristocrat/movies.
   If you would like to use a different path, change "mMoviePath" to your liking.
  *Some streaming formats are known to cause a crash. I know it is something to do with how
   I restart the animation when looping.
