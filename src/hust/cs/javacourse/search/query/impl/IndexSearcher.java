package hust.cs.javacourse.search.query.impl;

import hust.cs.javacourse.search.index.AbstractIndex;
import hust.cs.javacourse.search.index.AbstractPosting;
import hust.cs.javacourse.search.index.AbstractPostingList;
import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.impl.Index;
import hust.cs.javacourse.search.index.impl.Posting;
import hust.cs.javacourse.search.query.AbstractHit;
import hust.cs.javacourse.search.query.AbstractIndexSearcher;
import hust.cs.javacourse.search.query.Sort;
import hust.cs.javacourse.search.util.Config;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

// TODO
public class IndexSearcher extends AbstractIndexSearcher {
    public IndexSearcher() {
    }

    @Override
    public void open(String indexFile) {
        try {
            this.index.load(new File(indexFile));
        } catch (Exception err) {
            System.out.println(err);
        }
    }

    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        AbstractPostingList plist = this.index.search(queryTerm);
        Map<AbstractTerm, AbstractPosting> termPostingMapping = new HashMap<>();
        AbstractHit[] hits = new AbstractHit[plist.size()];
        for (int i = 0; i < plist.size(); i++) {
            AbstractPosting posting = plist.get(i);
            termPostingMapping.put(queryTerm, posting);
            hits[i] = new Hit(posting.getDocId(), this.index.getDocName(posting.getDocId()), termPostingMapping);       // 这里传文件名即可，不然会找不到文件，详情参见fileUtil中的read方法
            termPostingMapping.clear();
        }
        return hits;
    }

    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        AbstractPostingList plist1 = this.index.search(queryTerm1);
        AbstractPostingList plist2 = this.index.search(queryTerm2);
        Map<AbstractTerm, AbstractPosting> termPostingMapping = new HashMap<>();
        ArrayList<AbstractHit> hits = new ArrayList<>();
        switch (combine) {
            case OR:
                if (plist1 != null) {
                    for (int i = 0; i < plist1.size(); i++) {
                        AbstractPosting posting = plist1.get(i);
                        termPostingMapping.put(queryTerm1, posting);
                        hits.add(new Hit(posting.getDocId(), this.index.getDocName(posting.getDocId()), termPostingMapping));     // 这里传文件名即可，不然会找不到文件，详情参见fileUtil中的read方法
                        termPostingMapping.clear();
                    }
                }
                if (plist2 != null) {
                    for (int i = 0; i < plist2.size(); i++) {
                        AbstractPosting posting = plist2.get(i);
                        hits.forEach(item -> {
                            if (item.getDocId() == posting.getDocId()) {
                                item.getTermPostingMapping().put(queryTerm2, posting);
                            }
                        });
                        termPostingMapping.put(queryTerm2, posting);
                        hits.add(new Hit(posting.getDocId(), this.index.getDocName(posting.getDocId()), termPostingMapping));
                        termPostingMapping.clear();
                    }
                }
                break;
            case AND:
                if (plist1 != null && plist2 != null) {
                    for (int i = 0; i < plist1.size(); i++) {
                        AbstractPosting posting = plist1.get(i);
                        for (int j = 0; j < plist2.size(); j++) {
                            AbstractPosting posting1 = plist2.get(j);
                            if (posting.getDocId() == posting1.getDocId()) {
                                termPostingMapping.put(queryTerm1, posting);
                                termPostingMapping.put(queryTerm2, posting1);
                                hits.add(new Hit(posting.getDocId(), this.index.getDocName(posting.getDocId()), termPostingMapping));
                                termPostingMapping.clear();
                            }
                        }
                    }
                }
                break;
        }

        return hits.toArray(new AbstractHit[hits.size()]);
    }
}
