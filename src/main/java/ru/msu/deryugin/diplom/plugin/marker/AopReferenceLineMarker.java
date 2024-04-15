package ru.msu.deryugin.diplom.plugin.marker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.impl.source.PsiMethodImpl;
import ru.msu.deryugin.diplom.plugin.marker.call.AopMethodCallReferenceHandler;
import ru.msu.deryugin.diplom.plugin.marker.declaration.AopMethodDeclarationReferenceHandler;

import java.util.Collection;

public class AopReferenceLineMarker extends RelatedItemLineMarkerProvider {
    private final AopMethodCallReferenceHandler aopMethodCallReferenceHandler = new AopMethodCallReferenceHandler();
    private final AopMethodDeclarationReferenceHandler aopMethodDeclarationReferenceHandler = new AopMethodDeclarationReferenceHandler();
    @Override
    protected void collectNavigationMarkers(PsiElement element, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        // Inspection element must be method call or method declaration.
        if (!(element instanceof PsiMethodCallExpression) && !(element instanceof PsiMethodImpl)) {
            return;
        }

        if (element instanceof PsiMethodCallExpression) {
            aopMethodCallReferenceHandler.handle( (PsiMethodCallExpression) element, result);
        }

        if (element instanceof PsiMethodImpl) {
            aopMethodDeclarationReferenceHandler.handle( (PsiMethodImpl) element, result);
        }
    }
}
