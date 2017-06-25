//package it.albertus.cycles.gui.listener;
//
//import java.util.Map;
//
//import org.eclipse.draw2d.MouseEvent;
//import org.eclipse.draw2d.MouseListener;
//import org.eclipse.nebula.visualization.xygraph.figures.PlotArea;
//import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.Text;
//
//import it.albertus.cycles.engine.NumeralSystemProvider;
//import it.albertus.cycles.gui.FormProperty;
//import it.albertus.cycles.gui.SmallTorqueGraph;
//import it.albertus.cycles.model.BikesCfg;
//import it.albertus.cycles.model.Torque;
//
//public class TorqueGraphMouseListener implements MouseListener {
//
//	private final NumeralSystemProvider numeralSystemProvider;
//	private final SmallTorqueGraph graph;
//	private final Map<String, FormProperty> formProperties;
//
//	public TorqueGraphMouseListener(final SmallTorqueGraph graph, final Map<String, FormProperty> formProperties, final NumeralSystemProvider numeralSystemProvider) {
//		this.numeralSystemProvider = numeralSystemProvider;
//		this.graph = graph;
//		this.formProperties = formProperties;
//	}
//
//	@Override
//	public void mousePressed(final MouseEvent me) {
//		final XYGraph xyGraph = (XYGraph) ((PlotArea) me.getSource()).getParent();
//		final double rpm = xyGraph.getPrimaryXAxis().getPositionValue(me.getLocation().x, false) * 1000;
//		final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(graph.getBike().getType(), Torque.class, Torque.indexOf(rpm)));
//		if (formProperty != null) {
//			formProperty.getText().setFocus();
//		}
//	}
//
//	@Override
//	public void mouseDoubleClicked(final MouseEvent me) {
//		final XYGraph xyGraph = (XYGraph) ((PlotArea) me.getSource()).getParent();
//		final double rpm = xyGraph.getPrimaryXAxis().getPositionValue(me.getLocation().x, false) * 1000;
//		final FormProperty formProperty = formProperties.get(BikesCfg.buildPropertyKey(graph.getBike().getType(), Torque.class, Torque.indexOf(rpm)));
//		if (formProperty != null) {
//			final double val = xyGraph.getPrimaryYAxis().getPositionValue(me.getLocation().y, false);
//			final Text text = formProperty.getText();
//			text.setText(Long.toString(Math.max(0, Math.min(0xFF, Math.round(val))), numeralSystemProvider.getNumeralSystem().getRadix()));
//			text.notifyListeners(SWT.FocusOut, null);
//			text.setFocus();
//			text.selectAll();
//		}
//	}
//
//	@Override
//	public void mouseReleased(final MouseEvent me) {/* Ignore */}
//
//}
