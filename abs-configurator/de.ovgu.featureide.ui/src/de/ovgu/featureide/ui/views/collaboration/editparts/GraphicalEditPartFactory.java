/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2012  FeatureIDE team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.ui.views.collaboration.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import de.ovgu.featureide.ui.views.collaboration.model.Class;
import de.ovgu.featureide.ui.views.collaboration.model.Collaboration;
import de.ovgu.featureide.ui.views.collaboration.model.CollaborationModel;
import de.ovgu.featureide.ui.views.collaboration.model.Role;


/**
 * Creates editparts for given models.
 * 
 * @author Constanze Adler
 */
public class GraphicalEditPartFactory implements EditPartFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	//@Override
	public EditPart createEditPart(EditPart editPart, Object model) {
		if (model instanceof CollaborationModel)
			return new ModelEditPart((CollaborationModel) model);
		if (model instanceof Role)
			return new RoleEditPart((Role) model);
		if (model instanceof Class)
			return new ClassEditPart((Class) model);
		if (model instanceof Collaboration)
			return new CollaborationEditPart((Collaboration) model);
		return null;
	}

}
