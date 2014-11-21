/*******************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.swt.rtplot.internal;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.rtplot.Messages;
import org.csstudio.swt.rtplot.RTPlot;
import org.csstudio.swt.rtplot.SWTMediaPool;
import org.csstudio.swt.rtplot.Trace;
import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/** Dialog for adding annotation to a trace
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  @author Kay Kasemir
 */
public class AddAnnotationDialog<XTYPE extends Comparable<XTYPE>> extends Dialog
{
    final private RTPlot<XTYPE> plot;
    final private List<Trace<XTYPE>> traces;
    private Table trace_list;
    private Text text;

    public AddAnnotationDialog(final Shell shell, final RTPlot<XTYPE> plot)
    {
        super(shell);
        this.plot = plot;
        traces = new ArrayList<>();
        for (Trace<XTYPE> trace : plot.getTraces())
            traces.add(trace);
    }

    @Override
    protected void configureShell(final Shell shell)
    {
        super.configureShell(shell);
        shell.setText(Messages.AddAnnotation);
    }

    @Override
    protected boolean isResizable()
    {
        return true;
    }

    @Override
    protected Control createDialogArea(final Composite parent)
    {
        final Composite composite = (Composite) super.createDialogArea(parent);
        final SWTMediaPool media = new SWTMediaPool(parent);
        composite.setLayout(new GridLayout(2, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.AddAnnotation_Trace);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        trace_list = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        trace_list.setLinesVisible(true);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = 100;
        trace_list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        for (Trace<XTYPE> trace : traces)
        {
            final TableItem item = new TableItem(trace_list, SWT.NONE);
            item.setText(trace.getName());
            item.setForeground(media.get(trace.getColor()));
        }
        trace_list.setToolTipText(Messages.AddAnnotation_Trace_TT);
        trace_list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDoubleClick(MouseEvent e)
            {
                okPressed();
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Messages.AddAnnotation_Content);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

        text = new Text(composite, SWT.MULTI);
        text.setText(Messages.AddAnnotation_DefaultText);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = 100;
        text.setLayoutData(gd);
        text.setToolTipText(Messages.AddAnnotation_Text_TT);

        final Text info = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        info.setBackground(composite.getBackground());
        info.setText(Messages.AddAnnotation_Content_Help);
        info.setLayoutData(new GridData(SWT.FILL, 0, true, false, 2, 1));

        return composite;
    }

    @Override
    protected void okPressed()
    {
        plot.addAnnotation(traces.get(trace_list.getSelectionIndex()), text.getText());
        super.okPressed();
    }
}
