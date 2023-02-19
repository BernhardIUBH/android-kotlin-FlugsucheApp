package com.bernhard.flugsucheapp

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import com.bernhard.flugsucheapp.DetailRecyclerView.DetailActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


//Neue Keys für APIs:
//https://www.fakemail.net/
//https://locationiq.com/docs -- // 5000 Anfragen pro Tag - Ausreichend - Muss NICHT ausgetauscht werden
//https://app.goflightlabs.com/dashboard //100 Anfragen pro Tag

class MainActivity : AppCompatActivity() {
    private val apiAccessKeyLocationIQ = "pk.041053f002fe0366de7aa935d6a9fe14"
    private val apiAccessKeyAirLabs = "ee4d9846-c013-403e-a313-98d32704ebe1"
    private val client = OkHttpClient()
    var cal = Calendar.getInstance()
    val convertFormatView = SimpleDateFormat("dd.MM.yy", Locale.GERMAN)
    var convertFormatQuerry = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var edOrigin = findViewById<EditText>(R.id.edOrigin)
        val edDestination = findViewById<EditText>(R.id.edDestination)
        val btnDateDepature = findViewById<Button>(R.id.btnDateDepature)
        val btnDateArrival = findViewById<Button>(R.id.btnDateArrival)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val cvInfo = findViewById<CardView>(R.id.cvInfo)
        val tvInfo = findViewById<TextView>(R.id.tvInfo)
        val btnLocationOrigin = findViewById<ImageButton>(R.id.imgLocationOrigin)
        val btnLocationDestination = findViewById<ImageButton>(R.id.imgLocationDestination)
        var dateDepature = ""
        var dateArrival = ""
        var flightOrigin = ""
        var flightDestination = ""

        cvInfo.setVisibility(View.GONE)
        //Automatische Vorauswahl Flughafen des aktuellen Standortes
        fetchLocation(edOrigin)

//--IATA-Codes & Städte: Departure & Arrival -----------------------------------//
        btnLocationOrigin.setOnClickListener{
            fetchLocation(edOrigin)
        }
        btnLocationDestination.setOnClickListener{
            fetchLocation(edDestination)
        }

        edOrigin.doAfterTextChanged {
            flightOrigin = edOrigin.text.toString()
        }

        edDestination.doAfterTextChanged {
            flightDestination = edDestination.text.toString()
        }

//--Datum: Departure & Arrival -----------------------------------//
        btnDateDepature.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@MainActivity,
                    createDateSetListener(btnDateDepature),
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        })

        btnDateArrival.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(this@MainActivity,
                    createDateSetListener(btnDateArrival),
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        })

        btnDateDepature.doAfterTextChanged {
            checkDate(btnDateDepature.text , btnDateArrival.text, btnDateDepature, btnDateArrival, cvInfo, tvInfo)
            val parsedDate = convertFormatView.parse(btnDateDepature.text.toString())
            dateDepature = convertFormatQuerry.format(parsedDate).toString()
        }

        btnDateArrival.doAfterTextChanged {
            checkDate(btnDateDepature.text , btnDateArrival.text, btnDateDepature, btnDateArrival, cvInfo, tvInfo)
            val parsedDate = convertFormatView.parse(btnDateArrival.text.toString())
            dateArrival = convertFormatQuerry.format(parsedDate).toString()
        }

//--Flug suchen-----------------------------------//
        //btnDateDepature.setText("28.02.2023")
        //btnDateArrival.setText("02.03.2023")
        //edOrigin.setText("VIE")
        //edDestination.setText("MAD")
        btnSearch.setOnClickListener {
            if (checkDate(btnDateDepature.text , btnDateArrival.text, btnDateDepature, btnDateArrival, cvInfo, tvInfo) == true) {
                if (flightOrigin.subSequence(0,3) != flightDestination.subSequence(0,3)){
                    checkLoaction(flightOrigin, edOrigin)
                    checkLoaction(flightDestination, edDestination)
                    startActivity(Intent(this, DetailActivity::class.java).putExtra("IATA_ORIGIN", flightOrigin.subSequence(0,3)).putExtra("IATA_DESTINATION", flightDestination.subSequence(0,3)).putExtra("DATE_DEPATURE", dateDepature).putExtra("DATE_ARRIVAL", dateArrival))
                }
            }
        }
    }
    fun checkLoaction(userInput: String, ed: EditText)  {
        //WENN nicht --> Ortsnamecheck  --> API-Abfrage
        //ggf koardinaten ziehen
        var iata = ""
        var json : String? = null
        val inputStream : InputStream

        if(userInput==""){ }
        else {
            try {
                //Prüfen auf: IATA-Code
                if (userInput.length == 3) {
                    val userInputIATA = userInput.uppercase()
                    inputStream = assets.open("Airports.json")
                    json = inputStream.bufferedReader().use { it.readText() }.toString()
                    val obj = JSONObject(json)
                    val airportsArray = obj.getJSONArray("data")
                    for (i in 0 until airportsArray.length()) {
                        val l1 = airportsArray.getJSONObject(i)
                        if (l1.getString("codeIataAirport") == userInputIATA) {
                            iata = userInputIATA
                            ed.setText(iata)
                            break
                        }
                    }
                    inputStream.close()
                } else {
                    val request = Request.Builder()
                        .url("https://eu1.locationiq.com/v1/search?key=${apiAccessKeyLocationIQ}&q=${userInput}&format=json")
                        .get()
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }
                        override fun onResponse(call: Call, response: Response) {
                            if(response.code == 200) {      //Response Code 200 == API gibt eine Antwort
                                val body = response.body!!.string()
                                if(body.contains("{\"error\":")){
                                    //Key muss erneuert werden oder kein Ergebnis
                                }
                                else{
                                    //val json = inputStream.bufferedReader().use { it.readText() }.toString()
                                    var lat = 0.0
                                    var lon = 0.0
                                    val obj = JSONArray(body)
                                    val l1 = obj.getJSONObject(0)
                                    lat = l1.getDouble("lat")
                                    lon = l1.getDouble("lon")
                                    nextAirtportIataWithCooardinates(lat, lon, ed)
                                }
                                response.close()
                            }
                        }
                    })
                }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
            }
        }
    }

    private fun nextAirtportIataWithCooardinates(latitudeCity: Double, longitudeCity: Double, ed: EditText) {
        var nameAirport = ""
        var codeIataAirport = ""
        var distance = 0.0
        val request = Request.Builder()
            .url("https://airlabs.co/api/v9/nearby?lat=$latitudeCity&lng=$longitudeCity&distance=100".plus("&api_key=${apiAccessKeyAirLabs}"))
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response) {
                if(response.code == 200) {      //Response Code 200 == API gibt eine Antwort
                    val body = response.body!!.string()
                    if(body.contains("{\"error\":")){
                        //Key muss erneuert werden oder kein Ergebnis
                    }
                    else{
                        //val json = inputStream.bufferedReader().use { it.readText() }.toString()
                        val obj = JSONObject(body)
                        val objResponse = obj.getJSONObject("response")
                        val airportsArray = objResponse.getJSONArray("airports")
                        var smallestdistance = 100.0
                        for (i in 0 until airportsArray.length()) {
                            val l1 = airportsArray.getJSONObject(i)
                            if(l1.has("iata_code")) {
                                if(l1.getString("name").contains("Airport")) {
                                    if(l1.getDouble("distance") < smallestdistance) {
                                        nameAirport = l1.getString("name")
                                        codeIataAirport = l1.getString("iata_code")
                                        distance = l1.getDouble("distance")
                                        smallestdistance = distance
                                        ed.setText("$codeIataAirport - $nameAirport")
                                    }
                                }
                            }
                        }
                    }
                    response.close()
                }
            }
        })
    }

    private fun fetchLocation(ed: EditText) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                CancellationTokenSource().token
            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    val lat = location.latitude
                    val lon = location.longitude
                    nextAirtportIataWithCooardinates(lat, lon, ed)
                }
            }
    }

    private fun checkDate(dateDeparture: CharSequence, dateArrival: CharSequence, btnDateDepature: Button, btnDateArrival: Button, cvInfo: CardView, tvInfo: TextView) : Boolean {
        btnDateArrival.setBackgroundColor(Color.parseColor("#EEEEEE"))
        btnDateDepature.setBackgroundColor(Color.parseColor("#EEEEEE"))
        cvInfo.setVisibility(View.GONE)
        if (isValidDate(dateDeparture.toString())==true){
            if (isValidDate(dateArrival.toString())==true){
                if (convertFormatView.parse(dateDeparture.toString()) < convertFormatView.parse(dateArrival.toString())){
                }
                else {
                    btnDateArrival.setBackgroundColor(Color.parseColor("#EA5454"))
                    cvInfo.setVisibility(View.VISIBLE)
                    tvInfo.text = "Ankunfsdatum prüfen!"
                    return false
                }
            }
            else {
                btnDateArrival.setBackgroundColor(Color.parseColor("#EA5454"))
                cvInfo.setVisibility(View.VISIBLE)
                tvInfo.text = "Ankunfsdatum prüfen!"
                return false
            }
        }
        else {
            btnDateDepature.setBackgroundColor(Color.parseColor("#EA5454"))
            cvInfo.setVisibility(View.VISIBLE)
            tvInfo.text = "Abflugsdatum prüfen!"
            return false
        }
        return true
    }

    fun isValidDate(dateStr: String?): Boolean {
        convertFormatView.setLenient(false)
        try {
            convertFormatView.parse(dateStr)
        } catch (e: ParseException) {
            return false
        }
        return true
    }

    private fun createDateSetListener(btnDate: Button): DatePickerDialog.OnDateSetListener {
        val createDateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                btnDate.setText(convertFormatView.format(cal.getTime()))
                btnDate.setTextColor(Color.BLACK)
            }
        }
        return createDateSetListener
    }

}