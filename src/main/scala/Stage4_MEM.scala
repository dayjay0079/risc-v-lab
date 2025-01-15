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

  val address = io.data_in.asUInt & "xFFFFFFFC".U
  val byte_offset = io.data_in(1, 0)
  val write_data = VecInit(io.data_write(7, 0).asSInt, io.data_write(15, 8).asSInt,
                           io.data_write(23, 16).asSInt, io.data_write(31, 24).asSInt)
  val write_enable = VecInit(Seq.fill(4)(WireDefault(false.B)))

  val load_byte = WireDefault(0.S(8.W))
  val load_halfword = WireDefault(0.S(16.W))
  val load_word = WireDefault(0.S(32.W))
  val load_data = WireDefault(0.S(32.W))


  for(i <- 0 to 3) {
    data_memory(i).io.address := address
    data_memory(i).io.data_in := 0.S
    data_memory(i).io.write_enable := 0.B
  }

  // Store Logic
  switch(io.ctrl_in.store_type) {
    is(STORE_BYTE) {
      switch(byte_offset) {
        is(0.U) {
          data_memory(0).io.data_in := write_data(0.U)
          data_memory(0).io.write_enable := true.B
        }
        is(1.U) {
          data_memory(1).io.data_in := write_data(0.U)
          data_memory(1).io.write_enable := true.B
        }
        is(2.U) {
          data_memory(2).io.data_in := write_data(0.U)
          data_memory(2).io.write_enable := true.B
        }
        is(3.U) {
          data_memory(3).io.data_in := write_data(0.U)
          data_memory(3).io.write_enable := true.B
        }
      }
    }
    is(STORE_HALFWORD) {
      switch(byte_offset) {
        is(0.U) {
          data_memory(0).io.data_in := write_data(0.U)
          data_memory(1).io.data_in := write_data(1.U)
          data_memory(0).io.write_enable := true.B
          data_memory(1).io.write_enable := true.B
        }
        is(1.U) {
          data_memory(1).io.data_in := write_data(0.U)
          data_memory(2).io.data_in := write_data(1.U)
          data_memory(1).io.write_enable := true.B
          data_memory(2).io.write_enable := true.B
        }
        is(2.U) {
          data_memory(2).io.data_in := write_data(0.U)
          data_memory(3).io.data_in := write_data(1.U)
          data_memory(2).io.write_enable := true.B
          data_memory(3).io.write_enable := true.B
        }
      }
    }
    is(STORE_WORD) {
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
  val sign_extension : Bool = 1.B //io.ctrl_in.load_type(2)
  val load_type = RegNext(io.ctrl_in.load_type(1,0))
  val byte_offset_load = RegNext(byte_offset)

  switch(load_type) {
    is(0.U) {
      load_data := 0.S
    }
    is(LOAD_BYTE) {
      switch(byte_offset_load) {
        is(0.U) { load_byte := data_memory(0).io.data_out }
        is(1.U) { load_byte := data_memory(1).io.data_out }
        is(2.U) { load_byte := data_memory(2).io.data_out }
        is(3.U) { load_byte := data_memory(3).io.data_out }
      }
      load_data := Mux(sign_extension, Cat(load_byte, 0.U(24.W)).asSInt >> 24.U,
                                       Cat(0.U(24.W), load_byte).asSInt)
    }
    is(LOAD_HALFWORD) {
      switch(byte_offset_load) {
        is(0.U) { load_halfword := Cat(data_memory(1).io.data_out, data_memory(0).io.data_out).asSInt }
        is(1.U) { load_halfword := Cat(data_memory(2).io.data_out, data_memory(1).io.data_out).asSInt }
        is(2.U) { load_halfword := Cat(data_memory(3).io.data_out, data_memory(2).io.data_out).asSInt }
      }
      load_data := Mux(sign_extension, Cat(load_halfword, 0.U(16.W)).asSInt >> 16.U,
                                       Cat(0.U(16.W), load_halfword).asSInt)
    }
    is(LOAD_WORD) {
      load_word := Cat(data_memory(3).io.data_out, data_memory(2).io.data_out,
                       data_memory(1).io.data_out, data_memory(0).io.data_out).asSInt
      load_data := load_word
    }
  }

  // Output
  io.data_out_mem := load_data
  io.data_out_alu := RegNext(io.data_in)
  io.rd_out := RegNext(io.rd_in)
  io.ctrl_out := RegNext(io.ctrl_in)
}
