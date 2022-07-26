package com.imback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.imback.common.BaseContext;
import com.imback.common.Result;
import com.imback.entity.AddressBook;
import com.imback.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 移动端用户地址管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增用户地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public Result<AddressBook> save(@RequestBody AddressBook addressBook) {

        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    /**
     * 查询全部地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> selectAll(AddressBook addressBook) {
        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != BaseContext.getCurrentId(), AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> addressBookList = addressBookService.list(queryWrapper);
        return Result.success(addressBookList);
    }


    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public Result<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("addressBook = {}",addressBook);
        //先把用户名下的地址都设置成 0可选地址，再把选中的地址设置为默认地址1
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BaseContext.getCurrentId() != null,AddressBook::getUserId, BaseContext.getCurrentId());
        updateWrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(updateWrapper);

        addressBook.setIsDefault(1); //1默认地址 0可选地址
        addressBookService.updateById(addressBook);

        return Result.success(addressBook);
    }

    /**
     * 根据Id查询需要修改的地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id) {
        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq( AddressBook::getId, id);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        return Result.success(addressBook);
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping()
    public Result<AddressBook> update(@RequestBody AddressBook addressBook){
        log.info("addressBook = {}",addressBook);
        addressBookService.updateById(addressBook);
        return Result.success(addressBook);
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook> getDefault(){
        //根据用户Id查询 isdefault=1(默认地址)
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseContext.getCurrentId() != null,AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook addressBookServiceOne = addressBookService.getOne(queryWrapper);

        return Result.success(addressBookServiceOne);
    }
}
