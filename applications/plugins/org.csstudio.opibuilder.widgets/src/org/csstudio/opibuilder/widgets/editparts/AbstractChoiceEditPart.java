/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.widgets.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.csstudio.data.values.IEnumeratedMetaData;
import org.csstudio.data.values.IValue;
import org.csstudio.data.values.ValueUtil;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.ExecutionMode;
import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.properties.IWidgetPropertyChangeHandler;
import org.csstudio.opibuilder.util.OPIColor;
import org.csstudio.opibuilder.widgets.model.AbstractChoiceModel;
import org.csstudio.opibuilder.widgets.model.ChoiceButtonModel;
import org.csstudio.platform.ui.util.CustomMediaFactory;
import org.csstudio.swt.widgets.figures.AbstractChoiceFigure;
import org.csstudio.swt.widgets.figures.AbstractChoiceFigure.IChoiceButtonListener;
import org.csstudio.swt.widgets.figures.ChoiceButtonFigure;
import org.csstudio.utility.pv.PV;
import org.csstudio.utility.pv.PVListener;
import org.eclipse.draw2d.IFigure;

/**The abstract editpart of choice widget.
 *
 * @author Xihui Chen
 *
 */
public abstract class AbstractChoiceEditPart extends AbstractPVWidgetEditPart {

	private PVListener loadItemsFromPVListener;

	private IEnumeratedMetaData meta = null;


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IFigure doCreateFigure() {
		final AbstractChoiceModel model = getWidgetModel();
		updatePropSheet(model.isItemsFromPV());
		AbstractChoiceFigure choiceFigure = createChoiceFigure();
		choiceFigure.setSelectedColor(
				getWidgetModel().getSelectedColor().getSWTColor());

		choiceFigure.setFont(CustomMediaFactory.getInstance().getFont(
						model.getFont().getFontData()));

		choiceFigure.setHorizontal((Boolean)(model.getPropertyValue(AbstractChoiceModel.PROP_HORIZONTAL)));
		if(!model.isItemsFromPV() || getExecutionMode() == ExecutionMode.EDIT_MODE){
			List<String> items = getWidgetModel().getItems();
			if(items != null)
				choiceFigure.setStates(items);
		}

		choiceFigure.addChoiceButtonListener(new IChoiceButtonListener() {

			public void buttonPressed(int index, String value) {
				setPVValue(AbstractChoiceModel.PROP_PVNAME, value);
			}
		});

		markAsControlPV(AbstractPVWidgetModel.PROP_PVNAME, AbstractPVWidgetModel.PROP_PVVALUE);

		return choiceFigure;
	}

	protected abstract AbstractChoiceFigure createChoiceFigure();



	@Override
	public AbstractChoiceModel getWidgetModel() {
		return (AbstractChoiceModel)getModel();
	}

	@Override
	protected void doActivate() {
		super.doActivate();
		registerLoadItemsListener();
	}

	/**
	 *
	 */
	private void registerLoadItemsListener() {
		//load items from PV
		if(getExecutionMode() == ExecutionMode.RUN_MODE){
			if(getWidgetModel().isItemsFromPV()){
				PV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
				if(pv != null){
					if(loadItemsFromPVListener == null)
						loadItemsFromPVListener = new PVListener() {
							public void pvValueUpdate(PV pv) {
								IValue value = pv.getValue();
								if (value != null && value.getMetaData() instanceof IEnumeratedMetaData){
									IEnumeratedMetaData new_meta = (IEnumeratedMetaData)value.getMetaData();
									if(meta  == null || !meta.equals(new_meta)){
										meta = new_meta;
										List<String> itemsFromPV = new ArrayList<String>();
										for(String writeValue : meta.getStates()){
											itemsFromPV.add(writeValue);
										}
										getWidgetModel().setPropertyValue(
												AbstractChoiceModel.PROP_ITEMS, itemsFromPV);
									}
								}
							}
							public void pvDisconnected(PV pv) {}
						};
					pv.addListener(loadItemsFromPVListener);
				}
			}
		}
	}

	@Override
	protected void doDeActivate() {
		super.doDeActivate();
		if(getWidgetModel().isItemsFromPV()){
			PV pv = getPV(AbstractPVWidgetModel.PROP_PVNAME);
			if(pv != null && loadItemsFromPVListener != null){
				pv.removeListener(loadItemsFromPVListener);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void registerPropertyChangeHandlers() {
		IWidgetPropertyChangeHandler pvNameHandler = new IWidgetPropertyChangeHandler() {

			public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
				registerLoadItemsListener();
				return false;
			}
		};
		setPropertyChangeHandler(AbstractPVWidgetModel.PROP_PVNAME, pvNameHandler);


		getFigure().setEnabled(getWidgetModel().isEnabled() &&
				(getExecutionMode() == ExecutionMode.RUN_MODE));

		removeAllPropertyChangeHandlers(AbstractWidgetModel.PROP_ENABLED);

		//enable
		IWidgetPropertyChangeHandler enableHandler = new IWidgetPropertyChangeHandler(){
			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				if(getExecutionMode() == ExecutionMode.RUN_MODE)
					figure.setEnabled((Boolean)newValue);
				return false;
			}
		};
		setPropertyChangeHandler(AbstractWidgetModel.PROP_ENABLED, enableHandler);

		// PV_Value
		IWidgetPropertyChangeHandler pvhandler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue, final IFigure refreshableFigure) {
				if(newValue != null && newValue instanceof IValue){
					String stringValue = ValueUtil.getString((IValue)newValue);
					((AbstractChoiceFigure)refreshableFigure).setState(stringValue);
				}
				return false;
			}
		};
		setPropertyChangeHandler(AbstractChoiceModel.PROP_PVVALUE, pvhandler);

		// Items
		IWidgetPropertyChangeHandler itemsHandler = new IWidgetPropertyChangeHandler() {
			@SuppressWarnings("unchecked")
			public boolean handleChange(final Object oldValue,
					final Object newValue, final IFigure refreshableFigure) {
				if(newValue != null && newValue instanceof List){
					((AbstractChoiceFigure)refreshableFigure).setStates(
							((List<String>)newValue));
					if(getWidgetModel().isItemsFromPV())
						((AbstractChoiceFigure)refreshableFigure).
							setState(ValueUtil.getString(getPVValue(AbstractPVWidgetModel.PROP_PVNAME)));
				}
				return true;
			}
		};
		setPropertyChangeHandler(AbstractChoiceModel.PROP_ITEMS, itemsHandler);

		IWidgetPropertyChangeHandler selectedColorHandler = new IWidgetPropertyChangeHandler() {

			public boolean handleChange(Object oldValue, Object newValue, IFigure figure) {
				((ChoiceButtonFigure)figure).setSelectedColor(((OPIColor)newValue).getSWTColor());
				return false;
			}
		};

		setPropertyChangeHandler(ChoiceButtonModel.PROP_SELECTED_COLOR, selectedColorHandler);

		IWidgetPropertyChangeHandler horizontalHandler = new IWidgetPropertyChangeHandler() {

			public boolean handleChange(Object oldValue, Object newValue,
					IFigure figure) {
				((AbstractChoiceFigure)figure).setHorizontal((Boolean)newValue);
				return true;
			}
		};

		setPropertyChangeHandler(AbstractChoiceModel.PROP_HORIZONTAL, horizontalHandler);

		final IWidgetPropertyChangeHandler handler = new IWidgetPropertyChangeHandler() {
			public boolean handleChange(final Object oldValue,
					final Object newValue, final IFigure refreshableFigure) {
				if(!(Boolean)newValue)
					((AbstractChoiceFigure)refreshableFigure).setStates(
							(getWidgetModel().getItems()));
				updatePropSheet((Boolean) newValue);
				return false;
			}
		};
		getWidgetModel().getProperty(AbstractChoiceModel.PROP_ITEMS_FROM_PV).
			addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					handler.handleChange(evt.getOldValue(), evt.getNewValue(), getFigure());
				}
		});

	}

		/**
		* @param actionsFromPV
		*/
	private void updatePropSheet(final boolean itemsFromPV) {
		getWidgetModel().setPropertyVisible(
				AbstractChoiceModel.PROP_ITEMS, !itemsFromPV);
	}

	@Override
	public String getValue() {
		return ((AbstractChoiceFigure)getFigure()).getState();
	}

	@Override
	public void setValue(Object value) {
		if(value instanceof String)
			((AbstractChoiceFigure)getFigure()).setState((String)value);
		else if (value instanceof Integer)
			((AbstractChoiceFigure)getFigure()).setState((Integer)value);	}

}
