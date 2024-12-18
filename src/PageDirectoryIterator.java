import java.io.IOException;

public class PageDirectoryIterator {
 
    private Relation relationCourante;
    private int indexDataPageCourante;
    private int nbDataPages;

    public PageDirectoryIterator (Relation relationCourante)     {
        this.relationCourante = relationCourante;
        this.indexDataPageCourante = -1;
        MyBuffer bufferDataPage = null;
        try {
            bufferDataPage = this.relationCourante.getBufferManager().getPage(this.relationCourante.getHeaderPageId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bufferDataPage == null){
            bufferDataPage = new MyBuffer(this.relationCourante.getHeaderPageId(), relationCourante.getDiskManager().getDBConfig().getPageSize(), relationCourante.getBufferManager().getTimeCount());
        }
        this.nbDataPages = bufferDataPage.getInt(0);
        relationCourante.getBufferManager().freePage(this.relationCourante.getHeaderPageId(), false);
    }

    public PageId GetNextDataPageId()   {
        this.indexDataPageCourante+=1;
        if (this.indexDataPageCourante == this.nbDataPages) {
            return null;
        }
        MyBuffer bufferDataPage = null;
        try {
            bufferDataPage = this.relationCourante.getBufferManager().getPage(this.relationCourante.getHeaderPageId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bufferDataPage == null){
            bufferDataPage = new MyBuffer(this.relationCourante.getHeaderPageId(), relationCourante.getDiskManager().getDBConfig().getPageSize(), relationCourante.getBufferManager().getTimeCount());
        }
        bufferDataPage.position(4+(12*this.indexDataPageCourante));
        int fileIdx = bufferDataPage.getInt();
        int pageIdx = bufferDataPage.getInt();
        this.relationCourante.getBufferManager().freePage(this.relationCourante.getHeaderPageId(), false);
        return new PageId(fileIdx, pageIdx);
    }

    public void reset() {
        this.indexDataPageCourante = -1;
    }

    public void close() {
        this.indexDataPageCourante = -1;
        this.nbDataPages = 0;
    }
}

