package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.math.RoundingMode
import java.util.SortedMap

class MainActivity : ComponentActivity() , OnItemSelectedListener , View.OnClickListener{

    var selecteditem=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)


        val yearspinner: Spinner = findViewById(R.id.yearspinner)
        yearspinner.onItemSelectedListener = this
//        val statusspinner:Spinner = findViewById(R.id.fillingstatusspinner)
        val ad: ArrayAdapter<*> = ArrayAdapter.createFromResource(this,R.array.yearlist
        , android.R.layout.simple_spinner_item)
        ad.setDropDownViewResource(android.R.layout.simple_spinner_item)
        yearspinner.adapter = ad;


        val status: Spinner = findViewById(R.id.status)
        status.onItemSelectedListener = this
        val ad1: ArrayAdapter<*> = ArrayAdapter.createFromResource(this,R.array.fillingstatuslist
            , android.R.layout.simple_spinner_item)
        ad1.setDropDownViewResource(android.R.layout.simple_spinner_item)
        status.adapter = ad1;

         val calcbutton: Button = findViewById(R.id.button)
        calcbutton.setOnClickListener(this)

     }

    /**
     *
     */
    override fun onItemSelected(parent: AdapterView<*>?,
                                view: View, position: Int,
                                id: Long) {
        if (parent != null) {
            this.selecteditem = parent.getItemAtPosition(position).toString()
        };
        val standardDeduction: Int? = getStausKeyFromValueMap(this.selecteditem)?.let { getstandardByStatus(it) }
        val standarDedText:TextView = findViewById(R.id.stanardDeduction)
        standarDedText.setText(" "+standardDeduction)
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {}

    /**
     *
     */
    override fun onClick(v: View?) {
        val taxableIncome: EditText = findViewById(R.id.taxableIncome);
        val selectedStatus:Spinner = findViewById(R.id.status)
        val givenTaxableIncome = taxableIncome.text.toString().toDouble();
        val triple = getPercentageStep(givenTaxableIncome , selectedStatus.selectedItem.toString() )
        val percentage :Int = triple.first.toInt();
        val stepPercentageAmount :Double = triple.second;
        val reducedIncome: Double = triple.third;
        var tax:Double = 0.0
        if(stepPercentageAmount==0.0 && reducedIncome==0.0){
            tax = givenTaxableIncome * (percentage.toDouble()/100)
        }else{
            tax = stepPercentageAmount + (reducedIncome *  (percentage.toDouble()/100))
        }

        displayResults(tax , givenTaxableIncome , percentage);

    }

    /**
     *
     */
    fun displayResults(tax:Double , salary: Double , marginalTaxRate:Int) {
        var displaytext : TextView=findViewById(R.id.resultText)
        displaytext.setText("Your Tax Amount is $ "+ tax.toBigDecimal().setScale(2,RoundingMode.UP)  + "   \n");
        displaytext.append("Your Effective Tax Rate is " +
                ( (tax/salary) * 100 ).toBigDecimal().setScale(2, RoundingMode.UP) + " % \n");
        displaytext.append("Your Marginal Tax Rate is " +marginalTaxRate + "%")
    }


    /**
     *
     */
    fun getPercentageStep(salary: Double , status:String): Triple<String,Double,Double> {
         var sum : Double = 0.0
         var previousVal = 0.0;
         var finalAmount = 0.0;
         for ((k, v) in getMap(status)) {
             println("$k = $v")
             if(k - salary >=0){
               return Triple(v.toString() , sum , finalAmount);
             }else{
                 finalAmount = salary - k.toDouble();
                sum += (k-previousVal) * (v.toString().toDouble()/100.0);
                 previousVal = k.toDouble()+1;
             }
         }
         return Triple("37" , sum , finalAmount);

     }

    /**
     * This map return the all the percentage by
     */

    fun getMap(status:String): SortedMap<Int, Any?> {
        val  pair = getStausKeyFromValueMap(status)?.let { getMapByStatus(it) }
        val firstStr = pair?.first;
        val secondStr = pair?.second;
        var amountList = emptyList<String>()
        var percentageList = emptyList<String>()
        if (firstStr != null) {
            amountList = firstStr.split(",")
        }
        if (secondStr != null) {
            percentageList = secondStr.split(",")
        }
        val map = hashMapOf<Int, Any?>()
        for ((i, item) in amountList.withIndex()) {
            map.put(amountList.get(i).toInt() , percentageList.get(i))
        }
        return map.toSortedMap();
    }

    /**
     *
     */
    fun getMapByStatus(status:String): Pair<String, String>? {
        val map = hashMapOf<String, Pair<String,String>>()
        map.put("S" , Pair("11000,44725,95375,182100,231251,578125,578126" , "10,12,22,24,32,35,37"))
        map.put("MFJ" , Pair("22000,89450,190750,364200,462500,693750,693751" , "10,12,22,24,32,35,37"))
        map.put("MFS" , Pair("11000,44725,95375,182100,231250,346875,346876" , "10,12,22,24,32,35,37"))
        map.put("HOH" , Pair("15700,59850,95350,182100,231250,578100,578101" , "10,12,22,24,32,35,37"))
        return map.get(status);
    }

    /**
     *
     */
    fun getstandardByStatus(key:String): Int? {
        val map = hashMapOf<String,Int>()
        map.put("S",13850)
        map.put("MFJ",27700)
        map.put("HOH",20800)
        map.put("MFS",13850)
        return map.get(key);
    }

    /**
     *
     */
    fun getStausKeyFromValueMap(key:String) : String? {
        val map = hashMapOf<String, String>()
        map.put("Single" , "S")
        map.put("Married Filling Joint" , "MFJ")
        map.put("Married Filling Seprate" , "MFS")
        map.put("Head Of Household", "HOH")
        return map.get(key);
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name Your Tax Estimation",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

