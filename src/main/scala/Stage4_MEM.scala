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
  val s_type = io.ctrl_in.opcode === "b0100011".U
  val STORE_BYTE = 0.U
  val STORE_HALFWORD = 1.U
  val STORE_WORD = 2.U

  val write_address = io.data_in.asUInt & "xFFFFFFFC".U
  val byte_offset = io.data_in(1, 0)
  val write_data = Seq(io.data_write(7, 0), io.data_write(15, 8), io.data_write(23, 16), io.data_write(31, 24))
  val write_enable = Seq.fill(4)(WireDefault(false.B))

  // Calculate data to write
  for(i <- 0 to 3) {
    when(s_type) {
      switch(io.ctrl_in.funct3) {
        is(STORE_BYTE) {
          write_enable(i) := i.U === byte_offset
        }
        is(STORE_HALFWORD) {
          write_enable(i) := (i.U === byte_offset) || ((i+1).U === byte_offset)
        }
        is(STORE_WORD) {
          write_enable(i) := true.B
        }
      }
    }
    data_memory(i).io.address := write_address + i.U
    data_memory(i).io.data_in := write_data(i)
    data_memory(i).io.write_enable := write_enable(i)
  }

  // Output
  io.data_out_mem := Cat(data_memory(3).io.data_out, data_memory(2).io.data_out,
                     data_memory(1).io.data_out, data_memory(0).io.data_out)
  io.data_out_alu := RegNext(io.data_in)
  io.rd_out := RegNext(io.rd_in)
  io.ctrl_out := RegNext(io.ctrl_in)
}
