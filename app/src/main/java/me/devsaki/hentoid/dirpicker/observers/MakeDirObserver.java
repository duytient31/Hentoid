package me.devsaki.hentoid.dirpicker.observers;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import me.devsaki.hentoid.dirpicker.events.OpFailedEvent;
import me.devsaki.hentoid.dirpicker.events.UpdateDirTreeEvent;
import me.devsaki.hentoid.dirpicker.model.DirTree;
import timber.log.Timber;

/**
 * Created by avluis on 06/12/2016.
 * Make Directory Observer
 */
public class MakeDirObserver implements Observer<File> {

    private final DirTree dirTree;
    private File newDir;

    public MakeDirObserver(DirTree dirTree) {
        this.dirTree = dirTree;
    }

    private boolean isNewDirInCurrentDir() {
        if (newDir == null) {
            return false;
        }

        File rootDir = dirTree.getRoot();
        File parentDirOfNewDir = newDir.getParentFile();

        return rootDir.getAbsolutePath().equals(parentDirOfNewDir.getAbsolutePath());
    }

    @Override
    public void onComplete() {
        if (isNewDirInCurrentDir()) {
            EventBus.getDefault().post(new UpdateDirTreeEvent(dirTree.getRoot()));
        }
        Timber.d("Make directory completed.");
    }

    @Override
    public void onError(Throwable e) {
        Timber.d("onError: %s", e.toString());
        EventBus.getDefault().post(new OpFailedEvent());
    }

    @Override
    public void onSubscribe(Disposable d) {
        // TODO is there something to do here ?
    }

    @Override
    public void onNext(File file) {
        newDir = file;
    }
}
