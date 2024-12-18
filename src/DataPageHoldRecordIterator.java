import java.io.IOException;
import java.nio.ByteBuffer;

public class DataPageHoldRecordIterator implements IRecordIterator  {
    
    private PageId dataPage;
    private MyBuffer buffer;
    private Relation relation;
    private int indexRecord;
    private int nbRecordsInDataPage;
   

    public DataPageHoldRecordIterator(PageId dataPage, Relation relation) {
        this.dataPage = dataPage;
        this.relation = relation;
        this.indexRecord = -1;
        try{
            this.buffer = this.relation.getBufferManager().getPage(dataPage);
        }   catch   (IOException e){
            e.printStackTrace();
        }
        if (this.buffer == null){
            this.buffer = new MyBuffer(this.relation.getHeaderPageId(), relation.getDiskManager().getDBConfig().getPageSize(), relation.getBufferManager().getTimeCount());
        }
        this.nbRecordsInDataPage = buffer.getInt((int)(relation.getDiskManager().getDBConfig().getPageSize())-8);
    }


    @Override
    public Record getNextRecord(){
        this.indexRecord +=1 ;
        if(this.indexRecord == nbRecordsInDataPage){
            return null;
        }
        int tailleMaximalePage = (int) this.relation.getDiskManager().getDBConfig().getPageSize();
        this.buffer.position((int) (tailleMaximalePage - ((this.indexRecord+2)*8)));
        int recordstartPosition = this.buffer.getInt();
        ByteBuffer dataPage = ByteBuffer.wrap(this.buffer.getData());
        Record record = new Record(this.relation.getNbColonnes());
        this.relation.readFromBuffer(record, dataPage, recordstartPosition);
        return record;
    }

    @Override
    public void reset(){
        this.indexRecord = -1;
    }

    @Override
    public void close(){
        this.relation.getBufferManager().freePage(this.dataPage, false);
        this.indexRecord=-1;
        this.nbRecordsInDataPage=0;
    }
}
