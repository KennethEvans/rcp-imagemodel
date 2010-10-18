package net.kenevans.gpxinspector.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory
{

    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(false);
        layout.setFixed(false);

        // layout.addStandaloneView(SampleView.ID, false, IPageLayout.LEFT,
        // 1.0f, editorArea);
        IFolderLayout folder = layout.createFolder("topFolder",
            IPageLayout.TOP, 0.5f, editorArea);
        folder.addView("net.kenevans.gpxinspector.gpxView");
    }

}
