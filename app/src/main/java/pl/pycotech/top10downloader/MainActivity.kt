package pl.pycotech.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageURL: String = ""

    override fun toString(): String {
        return """
            name: $name
            artist: $artist
            releaseDate: $releaseDate
            summary: $summary
            imageURL: $imageURL
        """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val downloadData by lazy { DownloadData(this, xmlListView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: called")
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=25/xml")
        Log.d(TAG, "onCreate: done")
    }

    override fun onDestroy() {
        super.onDestroy()
        //true allow for interrupt task if running
        downloadData.cancel(true)
    }

    companion object {
        //1st String: url, 2nd Progress bar, 3rd type of result
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"

            //For get rid of 'leak' message
            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplication = ParseApplication()
                parseApplication.parse(result)

//                val arrayAdapter = ArrayAdapter<FeedEntry>(propContext, R.layout.list_item, parseApplication.applications)
//                propListView.adapter = arrayAdapter
                val arrayAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplication.applications)
                propListView.adapter = arrayAdapter

            }


            override fun doInBackground(vararg url: String?): String {
                Log.d(TAG, "doInBackground: starts with ${url[0]}")
                val rssFeed = downloadXML(url[0])
                if(rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            private fun downloadXML(urlPath: String?): String {
                return URL(urlPath).readText()
            }
        }
    }
}
