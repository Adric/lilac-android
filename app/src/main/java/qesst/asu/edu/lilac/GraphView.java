package qesst.asu.edu.lilac;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;

// TODO: figure out why this isn't working
/*class IVFormatter implements ValueFormatter
{

	private DecimalFormat mFormat;

	public IVFormatter()
	{
		mFormat = new DecimalFormat("###,###,##0.0"); // use one decima
	}

	@Override
	public String getFormattedValue(float value)
	{
		return mFormat.format(value) + " $"; // append a dollar-sign
	}
}*/


/**
 * Object for maintaining and updating the graph
 */
public class GraphView
{
	// View the graph is contained in
	private View mView;

	// We only care about line charts
	private LineChart mChart;

	private Activity mParent;

	boolean mEndMeasurement;

	boolean mIsSweep;

	boolean mAllowNegativeCurrents;
	
	private final String TAG = "GraphView";

	public GraphView(LineChart chart, View view, Activity parent)
	{
		mChart = chart;
		mView = view;
		mParent = parent;

		mAllowNegativeCurrents = false;

		init();
	}

	private void init()
	{
		if (mChart == null)
		{
			mChart = (LineChart) mView.findViewById(R.id.iv_curve);
		}

		if (mChart == null)
		{
			Log.d(TAG, "Failed to initialize graph!");
			return;
		}

		mEndMeasurement = false;

		// Set the labels
		mChart.setDescription("");
		YAxis yAxis = mChart.getAxisLeft();
		yAxis.setDrawGridLines(true);
		yAxis.setDrawAxisLine(true);

		mChart.getAxisRight().setEnabled(false);

		XAxis xAxis = mChart.getXAxis();
		xAxis.setDrawGridLines(true);
		mChart.setDoubleTapToZoomEnabled(true);
		mChart.enableScroll(); // needed?
		mChart.setScaleEnabled(true);
		mChart.setAutoScaleMinMaxEnabled(true);
		mChart.setDragEnabled(true);
		mChart.setDragDecelerationEnabled(true);
		mChart.setPinchZoom(true);
		mChart.setClickable(true);
		//mChart.setExtraBottomOffset(5.f);
		//mChart.setExtraTopOffset(5.f);

		// Add empty data point so we can add more later
		LineData data = new LineData();
		data.setValueTextColor(Color.WHITE);
		//data.setValueFormatter(new IVFormatter());
		mChart.setData(data);
	}
	
	public void updateGraph(ModuleData md)
	{
		if (mView == null)
		{
			Log.e(TAG, "trying to update graph with null view! Returning!");
			return;
		}

		if (mChart == null)
		{
			Log.e(TAG, "Trying to update graph when mChart is null! Setting up object");
			// Create the chart
			init();
		}

		if (mEndMeasurement)
		{
			Log.d(TAG, "mEndMeasurement == true");
			return;
		}

		LineData data = mChart.getData();
		//data.setValueTextColor(Color.BLUE);
		//data.setValueTextSize(8.f);
		
		if (data != null)
		{
			LineDataSet set = data.getDataSetByIndex(0);
			// set.addEntry(...); // can be called as well

			if (set == null)
			{
				set = createSet();
				set.setDrawCircles(false);
				set.setColor(Color.BLUE);

				data.addDataSet(set);
			}

			// If current drops below 0, stop and return
			if (!mAllowNegativeCurrents && mIsSweep && (md.getCurrent() <= 0.001f))
			{
				if (!mEndMeasurement)
				{
					Log.e(TAG, "Trying to add negative current to graph: " + md.getCurrent() + ", stopping measurement and skipping");
					//mMessageSystem.write('C');

					// TODO: refactor this hack!
					((MainActivity) mParent).stopMeasurement();
					mEndMeasurement = true;
				}
				return;
			}


			// create formatter for appropriate x labels
			/*DecimalFormat df = new DecimalFormat("#.###");
			df.setRoundingMode(RoundingMode.CEILING);
			float xVal = Float.valueOf(df.format(md.getCurrent()));*/
			// use valueformatter class instead

			// add a new x-value first
			//if (mVoc < md.getVoltage()) mVoc = (float)md.getVoltage();

			data.addXValue(Double.toString(md.getVoltage()));
			Log.d(TAG, "Adding values: (" + Double.toString(md.getVoltage()) + ", " + md.getCurrent() + ")");

			data.addEntry(new Entry((float)md.getCurrent(), set.getEntryCount()), 0);

			// let the chart know it's data has changed
			mChart.notifyDataSetChanged();

			// limit the number of visible entries
			//mChart.setVisibleXRangeMaximum(120);
			// mChart.setVisibleYRange(30, AxisDependency.LEFT);

			// move to the latest entry
			mChart.moveViewToX(data.getXValCount()/* - 121*/);

			// this automatically refreshes the chart (calls invalidate())
			// mChart.moveViewTo(data.getXValCount()-7, 55f,
			// AxisDependency.LEFT);

			//mChart.invalidate();
		}
	}

	private LineDataSet createSet() 
	{
		LineDataSet set = new LineDataSet(null, "Dynamic Data");
		set.setAxisDependency(YAxis.AxisDependency.LEFT);
		set.setColor(ColorTemplate.getHoloBlue());
		set.setCircleColor(Color.WHITE);
		set.setLineWidth(2f);
		set.setCircleSize(4f);
		set.setFillAlpha(65);
		set.setFillColor(ColorTemplate.getHoloBlue());
		set.setHighLightColor(Color.rgb(244, 117, 117));
		set.setValueTextColor(Color.WHITE);
		set.setValueTextSize(9f);
		set.setDrawValues(false);
		return set;
	}

	public boolean saveToGallery(String fileName, int quality)
	{
		return mChart.saveToGallery(fileName, quality);
	}

	public void reset()
	{
		init();
		mChart.invalidate();
	}

	public boolean isSweep()
	{
		return mIsSweep;
	}

	public void setIsSweep(boolean isSweep)
	{
		this.mIsSweep = isSweep;
	}

	public void resetEndMeasurement()
	{
		mEndMeasurement = false;
	}

	public boolean getAllowNegativeCurrents()
	{
		return mAllowNegativeCurrents;
	}

	public void setAllowNegativeCurrents(boolean allowNegativeCurrents)
	{
		mAllowNegativeCurrents = allowNegativeCurrents;
	}
}
