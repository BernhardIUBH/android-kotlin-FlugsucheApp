package com.bernhard.flugsucheapp.DetailRecyclerView

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bernhard.flugsucheapp.DataClassFlight
import com.bernhard.flugsucheapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_detail.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch

class DetailActivity : AppCompatActivity() {
    //https://app.goflightlabs.com/dashboard -- // 100 Anfragen im Monat - Bei bedarf austauschen! --> Readme
    private val apiAccessKeyFlightLabs = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI0IiwianRpIjoiMzBiNDFiMjkxNTZkNTdiNjAyYTI2MjEzMzU3MjMyOTI0NzExYzg4NzRmODUzMWEyZGM2YmFlNjEzZDFmM2U0MjVmYzhiZWU4ODJlMDU0NjIiLCJpYXQiOjE2NzUxNzg1MjIsIm5iZiI6MTY3NTE3ODUyMiwiZXhwIjoxNzA2NzE0NTIyLCJzdWIiOiIxOTg5MyIsInNjb3BlcyI6W119.RVQkKEQ3YeL8o82VUbnqcwFUufb64HZBE0zKVmq1tjOE4dmmc-2oBBV1F1RZQr3QwIq9sStR0BIYiRlClQ4Tpw"
    private val client = OkHttpClient()

    var modelDetailList: MutableList<ModelDetail> = ArrayList()
    lateinit var modelDetail: ModelDetail
    lateinit var detailAdapter: DetailAdapter
    lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var btnSortCarrierZA: Button
    lateinit var btnSortCarrierAZ: Button
    lateinit var btnSortPriceAsc: Button
    lateinit var btnSortPriceDesc: Button
    lateinit var btnSortDateTimeDepatureAsc: Button
    lateinit var btnSortDateTimeDepatureDesc: Button
    lateinit var btnSortDateTimeArrivalAsc: Button
    lateinit var btnSortDateTimeArrivalDesc: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val flightsList: ArrayList<DataClassFlight> = ArrayList()
        val iataOrigin = intent.getStringExtra("IATA_ORIGIN")
        val iataDestination = intent.getStringExtra("IATA_DESTINATION")
        val dateDepature = intent.getStringExtra("DATE_DEPATURE")
        val dateArrival = intent.getStringExtra("DATE_ARRIVAL")
        val tvOriginLabel = findViewById<TextView>(R.id.tvOriginLabel)
        val tvDestinationLabel = findViewById<TextView>(R.id.tvDestinationLabel)
        tvOriginLabel.text = iataOrigin?.uppercase()
        tvDestinationLabel.text = iataDestination?.uppercase()

        rvListData.setHasFixedSize(true)
        rvListData.setLayoutManager(LinearLayoutManager(this))

        getResults(iataOrigin, iataDestination, flightsList, dateDepature, dateArrival)
        //checkData(flightsList)
        getListData(flightsList)
        setFilterData()
    }

    private fun getResults(iataOrigin: String?, iataDestination: String?, flightsList: ArrayList<DataClassFlight>, dateDepature: String?, dateArrival: String?) {
        val infoAmountAdults = "&adults=1"
        val amountAdults = 1
        val infoOrigin = "&origin=".plus(iataOrigin)
        val infoDestination = "&destination=".plus(iataDestination)
        val infoDepartureDate = "&departureDate=".plus(dateDepature)
        val request = Request.Builder()
            .url("https://app.goflightlabs.com/search-best-flights?access_key=".plus(apiAccessKeyFlightLabs).plus(infoAmountAdults).plus(infoOrigin).plus(infoDestination).plus(infoDepartureDate))
            .get()
            .build()
        val countDownLatch = CountDownLatch(1)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                countDownLatch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {      //Response Code 200 == API gibt eine Antwort
                    val body = response.body!!.string()
                    if (body.contains("\"API more than 100 requests\"")) {
                        val message =
                            "API-Error: Schlüssel erneuern! - Die API erlaubt nur 100 Abfragen im Monat - Lösung ( 1. Minute ): in der **Readme**!"
                    } else if (body.contains("No data found")) {
                        val message = "Kein Eintrag"
                    } else {
                        try {
                            val obj = JSONObject(body!!)
                            val l1 = obj.getJSONObject("data")
                            val bucketsArray = l1.getJSONArray("buckets")
                            for (i in 0 until bucketsArray.length()) {
                                val l2 = bucketsArray.getJSONObject(i)
                                val itemsArray = l2.getJSONArray("items")
                                var id = ""
                                var rawPrice = 0.00
                                var originName = ""
                                var originIata = ""
                                var destinationName = ""
                                var destinationIata = ""
                                var duration = 0
                                var departure = ""
                                var arrival = ""
                                var carrierName = ""
                                for (i in 0 until itemsArray.length()) {
                                    val l3 = itemsArray.getJSONObject(i)
                                    id = l3.getString("id")
                                    val layerPrice = l3.getJSONObject("price")
                                    rawPrice = layerPrice.getDouble("raw")
                                    val legsArray = l3.getJSONArray("legs")
                                    for (i in 0 until legsArray.length()) {
                                        val l4 = legsArray.getJSONObject(i)
                                        val layerOrigin = l4.getJSONObject("origin")
                                        originName = layerOrigin.getString("name")
                                        originIata = layerOrigin.getString("id")
                                        val layerDestination = l4.getJSONObject("destination")
                                        destinationName = layerDestination.getString("name")
                                        destinationIata = layerDestination.getString("id")
                                        duration = l4.getInt("durationInMinutes")
                                        departure = l4.getString("departure")
                                        arrival = l4.getString("arrival")
                                        val layerCarriers = l4.getJSONObject("carriers")
                                        val marketingArray = layerCarriers.getJSONArray("marketing")
                                        for (i in 0 until marketingArray.length()) {
                                            val l5 = marketingArray.getJSONObject(i)
                                            carrierName = l5.getString("name")
                                        }
                                    }
                                }
                                val flightDetails = DataClassFlight(
                                    id,
                                    rawPrice,
                                    originName,
                                    originIata,
                                    destinationName,
                                    destinationIata,
                                    duration,
                                    departure,
                                    arrival,
                                    carrierName
                                )
                                flightsList.add(flightDetails)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
    }


    private fun getListData(flightsList: ArrayList<DataClassFlight>) {
        for (i in flightsList.indices){
            modelDetail = ModelDetail(flightsList.get(i).departure, flightsList.get(i).arrival, flightsList.get(i).carrierName, flightsList.get(i).rawPrice)
            modelDetailList.add(modelDetail)
        }

        detailAdapter = DetailAdapter(modelDetailList)
        rvListData.adapter = detailAdapter
    }

    private fun checkData(flightsList: ArrayList<DataClassFlight>) {
        if(flightsList.isEmpty()){
            finish()
        }
    }

    //@SuppressLint("MissingInflatedId")
    private fun setFilterData() {
        fabFilter.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)

            btnSortCarrierAZ = dialogView.findViewById(R.id.btnCarrierAZ)
            btnSortCarrierZA = dialogView.findViewById(R.id.btnCarrierZA)
            btnSortPriceAsc = dialogView.findViewById(R.id.btnDateDepature)
            btnSortPriceDesc = dialogView.findViewById(R.id.btnDateArrival)
            btnSortDateTimeDepatureAsc = dialogView.findViewById(R.id.btnDateTimeDepatureAsc)
            btnSortDateTimeDepatureDesc = dialogView.findViewById(R.id.btnDateTimeDepatureDesc)
            btnSortDateTimeArrivalAsc = dialogView.findViewById(R.id.btnDateTimeArrivalAsc)
            btnSortDateTimeArrivalDesc = dialogView.findViewById(R.id.btnDateTimeArrivalDesc)

            bottomSheetDialog = BottomSheetDialog(this@DetailActivity)
            bottomSheetDialog.setContentView(dialogView)
            bottomSheetDialog.show()

            btnSortCarrierAZ.setOnClickListener {
                Collections.sort(modelDetailList, ModelDetail.sortByCarrierAZ)
                detailAdapter.notifyDataSetChanged()
                btnSortCarrierAZ.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortCarrierZA.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            btnSortCarrierZA.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByCarrierZA)
                detailAdapter.notifyDataSetChanged()
                btnSortCarrierZA.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortCarrierAZ.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            btnSortPriceAsc.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByPriceAsc)
                detailAdapter.notifyDataSetChanged()
                btnSortPriceAsc.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortPriceDesc.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            btnSortPriceDesc.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByPriceDesc)
                detailAdapter.notifyDataSetChanged()
                btnSortPriceDesc.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortPriceAsc.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            btnSortDateTimeDepatureAsc.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByDateTimeDepatureAsc)
                detailAdapter.notifyDataSetChanged()
                btnSortDateTimeDepatureAsc.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortDateTimeDepatureDesc.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            btnSortDateTimeDepatureDesc.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByDateTimeDepatureDesc)
                detailAdapter.notifyDataSetChanged()
                btnSortDateTimeDepatureDesc.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortDateTimeDepatureAsc.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            btnSortDateTimeArrivalAsc.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByDateTimeArrivalAsc)
                detailAdapter.notifyDataSetChanged()
                btnSortDateTimeArrivalAsc.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortDateTimeArrivalDesc.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            btnSortDateTimeArrivalDesc.setOnClickListener{
                Collections.sort(modelDetailList, ModelDetail.sortByDateTimeArrivalDesc)
                detailAdapter.notifyDataSetChanged()
                btnSortDateTimeArrivalDesc.setBackgroundColor(Color.parseColor("#53c2e2"))
                btnSortDateTimeArrivalAsc.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

        }
    }

}