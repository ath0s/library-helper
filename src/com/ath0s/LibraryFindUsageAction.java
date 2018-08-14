package com.ath0s;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.ide.projectView.impl.nodes.NamedLibraryElement;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.LibraryOrSdkOrderEntry;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.FindUsagesInProjectStructureActionBase;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.LibraryProjectStructureElement;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureElement;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class LibraryFindUsageAction extends AnAction {

    public LibraryFindUsageAction() {
        super(ProjectBundle.message("find.usages.action.text"), ProjectBundle.message("find.usages.action.text"), AllIcons.Actions.Find);
        registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_FIND_USAGES).getShortcutSet(), null);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        getLibrary(anActionEvent).ifPresent(library -> {
            ProjectStructureConfigurable projectStructureConfigurable = ProjectStructureConfigurable.getInstance(project);

            projectStructureConfigurable.disposeUIResources();
            JComponent jComponent = projectStructureConfigurable.createComponent();
            projectStructureConfigurable.reset();

            StructureConfigurableContext structureConfigurableContext = projectStructureConfigurable.getContext();

            new FindUsagesInProjectStructureActionBase(jComponent, project) {

                @Override
                protected boolean isEnabled() {
                    return true;
                }

                @Override
                protected ProjectStructureElement getSelectedElement() {
                    return new LibraryProjectStructureElement(structureConfigurableContext, library);
                }

                @Override
                protected StructureConfigurableContext getContext() {
                    return structureConfigurableContext;
                }

                @Override
                protected RelativePoint getPointToShowResults() {
                    ProjectViewTree component = (ProjectViewTree) anActionEvent.getData(DataKeys.CONTEXT_COMPONENT);
                    Rectangle rowBounds = component.getRowBounds(component.getLeadSelectionRow());
                    Point location = rowBounds.getLocation();
                    location.x += rowBounds.width;
                    return new RelativePoint(component, location);
                }
            }.actionPerformed(anActionEvent);
        });
    }

    @Override
    public void update(AnActionEvent anActionEvent) {
        anActionEvent.getPresentation().setEnabledAndVisible(getLibrary(anActionEvent).isPresent());
    }

    private Optional<Library> getLibrary(AnActionEvent anActionEvent) {
        NamedLibraryElement[] namedLibraryElements = anActionEvent.getData(NamedLibraryElement.ARRAY_DATA_KEY);
        if (namedLibraryElements != null && namedLibraryElements.length > 0) {
            NamedLibraryElement namedLibraryElement = namedLibraryElements[0];

            LibraryOrSdkOrderEntry libraryOrSdkOrderEntry = namedLibraryElement.getOrderEntry();
            if (libraryOrSdkOrderEntry instanceof LibraryOrderEntry) {
                LibraryOrderEntry libraryOrderEntry = (LibraryOrderEntry) libraryOrSdkOrderEntry;
                return Optional.ofNullable(libraryOrderEntry.getLibrary());
            }
        }
        return Optional.empty();
    }
}
