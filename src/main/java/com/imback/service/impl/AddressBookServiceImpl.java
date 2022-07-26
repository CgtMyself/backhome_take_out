package com.imback.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imback.common.BaseContext;
import com.imback.dao.AddressBookMapper;
import com.imback.entity.AddressBook;
import com.imback.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
