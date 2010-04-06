/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.chainsaw;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.chainsaw.helper.SwingHelper;


/**
 * A Sortable JTable implementation that allows a user to click on a
 * specific Column and have the row information sorted by that column.
 *
 * @author Claude Duguay
 * @author Scott Deboy <sdeboy@apache.org>
 * 
 */
public class JSortTable extends JTable implements MouseListener {
  protected int sortedColumnIndex = -1;
  protected boolean sortedColumnAscending = true;
  private String sortedColumn;

  public JSortTable() {
    super();
    initSortHeader();
  }
  public JSortTable(SortTableModel model) {
    super(model);
    initSortHeader();
  }

  public JSortTable(SortTableModel model, TableColumnModel colModel) {
    super(model, colModel);
    initSortHeader();
  }

  public JSortTable(
    SortTableModel model, TableColumnModel colModel,
    ListSelectionModel selModel) {
    super(model, colModel, selModel);
    initSortHeader();
  }

  protected void initSortHeader() {
    JTableHeader header = getTableHeader();
    header.setDefaultRenderer(new SortHeaderRenderer());
    header.addMouseListener(this);
  }

  public int getSortedColumnIndex() {
    return sortedColumnIndex;
  }

  public void updateSortedColumn() {
  	if (sortedColumn != null) {
  		try {
	  		sortedColumnIndex = columnModel.getColumnIndex(sortedColumn);
            getTableHeader().resizeAndRepaint();
  		} catch (IllegalArgumentException ie) {//nothing...column is not in the model
  			setSortedColumnIndex(-1);
	  	}
	  }
  }
  
  public void setSortedColumnIndex(int index) {
    sortedColumnIndex = index;
    if (sortedColumnIndex > -1) {
        SortTableModel model = (SortTableModel) getModel();
        model.sortColumn(sortedColumnIndex, sortedColumnAscending);
    }
    getTableHeader().resizeAndRepaint();
  }

  public void scrollTo(final int row, final int col) {
    SwingHelper.invokeOnEDT(new Runnable() {
      public void run() {
        if ((row > -1) && (row < getRowCount())) {
          try {
            setRowSelectionInterval(row, row);
            //get the requested row off of the bottom and top of the screen by making the 5 rows around the requested row visible
            scrollRectToVisible(getCellRect(row + 5, col, true));
            scrollRectToVisible(getCellRect(row - 5, col, true));
            scrollRectToVisible(getCellRect(row, col, true));
          } catch (IllegalArgumentException iae) {
            //ignore..out of bounds
          }
        }
      }
    });
  }

  public void scrollToRow(int row) {
    scrollTo(row, columnAtPoint(getVisibleRect().getLocation()));
  }

  public boolean isSortedColumnAscending() {
    return sortedColumnAscending;
  }

  public void mouseClicked(MouseEvent event) {
  	
  	if(event.getClickCount()<2 || event.isPopupTrigger()){
  		return;
  	}else if(event.getClickCount()>1 && ((event.getModifiers() & InputEvent.BUTTON2_MASK)>0)){
  		return;
  	}
  	
    TableColumnModel colModel = getColumnModel();
    int index = colModel.getColumnIndexAtX(event.getX());
    int modelIndex = colModel.getColumn(index).getModelIndex();
    SortTableModel model = (SortTableModel) getModel();

    if (model.isSortable(modelIndex)) {
      // toggle ascension, if already sorted
      if (sortedColumnIndex == index) {
        sortedColumnAscending = !sortedColumnAscending;
      }

      sortedColumnIndex = index;
      sortedColumn = colModel.getColumn(index).getHeaderValue().toString();
      model.sortColumn(modelIndex, sortedColumnAscending);
      getTableHeader().resizeAndRepaint();
    }
  }

  public void mousePressed(MouseEvent event) {
  }

  public void mouseReleased(MouseEvent event) {
  }

  public void mouseEntered(MouseEvent event) {
  }

  public void mouseExited(MouseEvent event) {
  }
}
