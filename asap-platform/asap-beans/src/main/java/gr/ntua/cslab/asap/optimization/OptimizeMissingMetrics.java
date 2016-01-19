package gr.ntua.cslab.asap.optimization;

import gr.ntua.cslab.asap.operators.SpecTree;
import gr.ntua.cslab.asap.operators.SpecTreeNode;
import gr.ntua.ece.cslab.panic.core.containers.beans.InputSpacePoint;
import gr.ntua.ece.cslab.panic.core.containers.beans.OutputSpacePoint;
import gr.ntua.ece.cslab.panic.core.models.MLPerceptron;
import gr.ntua.ece.cslab.panic.core.models.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

public class OptimizeMissingMetrics {

	public static OutputSpacePoint findOptimalPointCheckAllSamples(
			HashMap<String, List<Model>> models, InputSpacePoint in,
			String policy, SpecTree optree) throws Exception {
		for(Entry<String, Double> e : in.getValues().entrySet()){
			if(e.getValue()==null){
				SpecTreeNode node = optree.getNode("Optimization.inputSpace." + e.getKey());
				e.setValue(findOptimal(models, in, e.getKey(), node));
				optree.add("SelectedParam."+e.getKey(), "2.0");
			}
		}

		OutputSpacePoint out =  new OutputSpacePoint();
		out.setInputSpacePoint(in);
		for(String metric : models.keySet()){
			Model model = models.get(metric).get(0);
			if(!model.getClass().equals(gr.ntua.ece.cslab.panic.core.models.UserFunction.class)){
				for(Model m:models.get(metric)){

					if(m.getInputSpace().size()>=2 && m.getClass().equals(gr.ntua.ece.cslab.panic.core.models.MLPerceptron.class)){
						model =m;
						break;
					}
					if(m.getInputSpace().size()<2 && m.getClass().equals(gr.ntua.ece.cslab.panic.core.models.LinearRegression.class)){
						model =m;
						break;
					}
				}
			}
			//System.out.println("Mettric: "+metric+" Model selected: "+ model.getClass());


			OutputSpacePoint op =  new OutputSpacePoint();
			HashMap<String, Double> values = new HashMap<String, Double>();
			for(String k :  model.getOutputSpace().keySet()){
				values.put(k, null);
			}
			op.setValues(values);
			OutputSpacePoint res = model.getPoint(in,op);
			out.addValue(metric, res.getOutputPoints().get(metric));
			//System.out.println("Out1: "+out);
		}
		return out;
	}

	protected Double computePolicyFunction(HashMap<String,Double> metrics, String policy) throws NumberFormatException, EvaluationException {
		//System.out.println("Computing function "+ metrics);

		Evaluator evaluator = new Evaluator();
		Double res=0.0;
		String tempFunction = new String(policy);
		for(String m : metrics.keySet()){
			tempFunction=tempFunction.replace(m, metrics.get(m)+"");
		}
		res = Double.parseDouble(evaluator.evaluate(tempFunction));
		//System.out.println(res);
		return res;
	}
	protected static Double findOptimal(HashMap<String, List<Model>> models, InputSpacePoint in, String key, SpecTreeNode missing){
		double bestVal = 2.0;
		String miss[] = missing.toString()
				.replaceAll("\\(", "")
				.replaceAll("\\)", "")
				.split(",");
		Double min = Double.parseDouble(miss[2]);
		Double max = Double.parseDouble(miss[3]);
		Double step = Double.parseDouble(miss[4]);
		Model model = new MLPerceptron();
		HashMap<String, Double> tmpVal = new HashMap<String, Double>(in.getValues());
		InputSpacePoint tmpIn = new InputSpacePoint();

		tmpIn.setValues(tmpVal);

		//Get model
		for (Entry<String, List<Model>> entry : models.entrySet()){
			for (Model m : entry.getValue())
				model = m;
		}

		//Set null values to 0
		for (Entry<String, Double> point : tmpIn.getValues().entrySet()){
			if (point.getValue() == null) {point.setValue(5.0);}
		}

		try {
			//Get optimal
			double bestTime = Double.MAX_VALUE;
			double predicted;

			for (double i = min; i <= max; i += step) {
				tmpIn.getValues().put(key, i);
				predicted = model.getPoint(tmpIn).getValue();
				if (predicted < bestTime) {
					bestTime = predicted;
					bestVal = i;
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
		}

		return bestVal;
	}
}