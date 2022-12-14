package com.example.dpms_research_sample;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BST_Service {
    @Autowired
    private BSTRepository bstrepo;


    public List<BST> listAll(User username) {
        //return bstrepo.findAll();
         return bstrepo.search(username);
    }
    public void save(BST bst) {
        bstrepo.save(bst);
    }

    public BST get(long id) {
        return bstrepo.findById(id).get();
    }

    public void delete(long id) {
        bstrepo.deleteById(id);
    }
    public Page<BST> findlist(int pageNum,User user) {
        int pageSize = 10;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return bstrepo.searchList(user,pageable);
    }

}
