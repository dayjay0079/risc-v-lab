import chisel3._
import chisel3.util._
import lib.ControlBus

class Stage4_MEM(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle{
    val data_write = Input(SInt(32.W))
    val data_in = Input(SInt(32.W))
    val rd_in = Input(UInt(5.W))
    val ctrl_in = Input(new ControlBus)
    val data_out_mem = Output(SInt(32.W))
    val data_out_alu = Output(SInt(32.W))
    val rd_out = Output(UInt(5.W))
    val ctrl_out = Output(new ControlBus)
  })

  // Data memory
  val data_memory = Seq.fill(4)(Module(new MemoryData(fpga, mem_size)))

  // Calculate data to write
  val STORE_BYTE = 1.U
  val STORE_HALFWORD = 2.U
  val STORE_WORD = 3.U
  val LOAD_BYTE = 1.U
  val LOAD_HALFWORD = 2.U
  val LOAD_WORD = 3.U
  val LOAD_BYTE_U = 4.U
  val LOAD_HALFWORD_U = 5.U

  val address = (io.data_in.asUInt >> 2.U).asUInt
  val byte_offset = io.data_in(1, 0)
  val write_data = VecInit(io.data_write(7, 0).asSInt, io.data_write(15, 8).asSInt,
                           io.data_write(23, 16).asSInt, io.data_write(31, 24).asSInt)
  val write_enable = VecInit(Seq.fill(4)(WireDefault(false.B)))

  val load_data = WireDefault(0.S(32.W))
  val load_byte = WireDefault(0.U(8.W))
  val load_halfword = WireDefault(0.U(16.W))

  for(i <- 0 to 3) {
    data_memory(i).io.address := 0.U
    data_memory(i).io.data_in := 0.S
    data_memory(i).io.write_enable := 0.B
  }

  // Store Logic
  switch(io.ctrl_in.store_type) {
    is(STORE_BYTE) {
      switch(byte_offset) {
        is(0.U) {
          data_memory(0).io.address := address
          data_memory(0).io.data_in := write_data(0.U)
          data_memory(0).io.write_enable := true.B
        }
        is(1.U) {
          data_memory(1).io.address := address
          data_memory(1).io.data_in := write_data(0.U)
          data_memory(1).io.write_enable := true.B
        }
        is(2.U) {
          data_memory(2).io.address := address
          data_memory(2).io.data_in := write_data(0.U)
          data_memory(2).io.write_enable := true.B
        }
        is(3.U) {
          data_memory(3).io.address := address
          data_memory(3).io.data_in := write_data(0.U)
          data_memory(3).io.write_enable := true.B
        }
      }
    }
    is(STORE_HALFWORD) {
      switch(byte_offset) {
        is(0.U) {
          data_memory(0).io.address := address
          data_memory(1).io.address := address
          data_memory(0).io.data_in := write_data(0.U)
          data_memory(1).io.data_in := write_data(1.U)
          data_memory(0).io.write_enable := true.B
          data_memory(1).io.write_enable := true.B
        }
        is(1.U) {
          data_memory(1).io.address := address
          data_memory(2).io.address := address
          data_memory(1).io.data_in := write_data(0.U)
          data_memory(2).io.data_in := write_data(1.U)
          data_memory(1).io.write_enable := true.B
          data_memory(2).io.write_enable := true.B
        }
        is(2.U) {
          data_memory(2).io.address := address
          data_memory(3).io.address := address
          data_memory(2).io.data_in := write_data(0.U)
          data_memory(3).io.data_in := write_data(1.U)
          data_memory(2).io.write_enable := true.B
          data_memory(3).io.write_enable := true.B
        }
      }
    }
    is(STORE_WORD) {
      data_memory(0).io.address := address
      data_memory(1).io.address := address
      data_memory(2).io.address := address
      data_memory(3).io.address := address
      data_memory(0).io.data_in := write_data(0.U)
      data_memory(1).io.data_in := write_data(1.U)
      data_memory(2).io.data_in := write_data(2.U)
      data_memory(3).io.data_in := write_data(3.U)
      data_memory(0).io.write_enable := true.B
      data_memory(1).io.write_enable := true.B
      data_memory(2).io.write_enable := true.B
      data_memory(3).io.write_enable := true.B
    }
  }

  // Load Logic
  switch(io.ctrl_in.load_type) {
    is(LOAD_BYTE) {
      switch(byte_offset) {
        is(0.U) { load_byte := data_memory(0).io.data_out.asUInt }
        is(1.U) { load_byte := data_memory(1).io.data_out.asUInt }
        is(2.U) { load_byte := data_memory(2).io.data_out.asUInt }
        is(3.U) { load_byte := data_memory(3).io.data_out.asUInt }
      }
      load_data := Mux(load_byte(7).asBool, Cat("xFFFFFF".U(24.W), load_byte).asSInt, Cat(0.U(24.W), load_byte).asSInt)
    }
    is(LOAD_HALFWORD) {
      switch(byte_offset) {
        is(0.U) { load_halfword := Cat(data_memory(1).io.data_out, data_memory(0).io.data_out).asUInt }
        is(1.U) { load_halfword := Cat(data_memory(2).io.data_out, data_memory(1).io.data_out).asUInt }
        is(2.U) { load_halfword := Cat(data_memory(3).io.data_out, data_memory(2).io.data_out).asUInt }
      }
      load_data := Mux(load_halfword(15).asBool, Cat("xFFFF".U(16.W), load_byte).asSInt, Cat(0.U(16.W), load_byte).asSInt)
    }
    is(LOAD_WORD) {
      load_data := Cat(data_memory(3).io.data_out, data_memory(2).io.data_out,
                       data_memory(1).io.data_out, data_memory(0).io.data_out).asSInt
    }
    is(LOAD_BYTE_U) {
      switch(byte_offset) {
        is(0.U) { load_byte := data_memory(0).io.data_out.asUInt }
        is(1.U) { load_byte := data_memory(1).io.data_out.asUInt }
        is(2.U) { load_byte := data_memory(2).io.data_out.asUInt }
        is(3.U) { load_byte := data_memory(3).io.data_out.asUInt }
      }
      load_data := Cat(0.U(24.W), load_byte).asSInt
    }
    is(LOAD_HALFWORD_U) {
      switch(byte_offset) {
        is(0.U) { load_halfword := Cat(data_memory(1).io.data_out, data_memory(0).io.data_out).asUInt }
        is(1.U) { load_halfword := Cat(data_memory(2).io.data_out, data_memory(1).io.data_out).asUInt }
        is(2.U) { load_halfword := Cat(data_memory(3).io.data_out, data_memory(2).io.data_out).asUInt }
      }
      load_data := Cat(0.U(16.W), load_byte).asSInt
    }
  }

  // Output
  io.data_out_mem := load_data
  io.data_out_alu := RegNext(io.data_in)
  io.rd_out := RegNext(io.rd_in)
  io.ctrl_out := RegNext(io.ctrl_in)
}
